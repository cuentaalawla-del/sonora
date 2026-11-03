package com.example.player

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
class YouTubeAudioEngine private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: YouTubeAudioEngine? = null

        fun getInstance(context: Context): YouTubeAudioEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = YouTubeAudioEngine(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private var webView: WebView? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0)
    val durationMs: StateFlow<Int> = _durationMs.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    var onCompletionListener: (() -> Unit)? = null
    var onErrorListener: ((Int) -> Unit)? = null

    // YouTube rejects embeds (error 150/152/153) that have no valid Referer/origin or come from
    // youtube.com itself. We identify the app with its package name, as YouTube recommends.
    private val embedOrigin: String = "https://${context.packageName}"

    private var currentVideoId: String? = null
    private var isApiReady = false

    init {
        mainHandler.post {
            setupWebView(context)
        }
    }

    fun getOrCreateWebView(ctx: Context): WebView {
        if (webView == null) {
            setupWebView(ctx)
        }
        val wv = webView!!
        (wv.parent as? ViewGroup)?.removeView(wv)
        return wv
    }

    private fun setupWebView(ctx: Context) {
        try {
            val wv = WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.databaseEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isApiReady = true
                        currentVideoId?.let { vid ->
                            loadVideoInternal(vid)
                        }
                    }
                }

                addJavascriptInterface(AndroidYouTubeBridge(), "AndroidBridge")
            }

            wv.loadDataWithBaseURL(embedOrigin, getPlayerHtml(), "text/html", "UTF-8", null)
            webView = wv
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play(videoId: String) {
        currentVideoId = videoId
        _isBuffering.value = true
        _currentPositionMs.value = 0
        mainHandler.post {
            if (webView == null) {
                setupWebView(context)
            }
            if (isApiReady) {
                loadVideoInternal(videoId)
            }
        }
    }

    private fun loadVideoInternal(videoId: String) {
        val js = "javascript:playVideoById('$videoId');"
        webView?.evaluateJavascript(js, null)
    }

    fun resume() {
        mainHandler.post {
            webView?.evaluateJavascript("javascript:resumeVideo();", null)
            _isPlaying.value = true
        }
    }

    fun pause() {
        mainHandler.post {
            webView?.evaluateJavascript("javascript:pauseVideo();", null)
            _isPlaying.value = false
        }
    }

    fun seekTo(positionMs: Int) {
        val seconds = positionMs / 1000f
        mainHandler.post {
            webView?.evaluateJavascript("javascript:seekToTime($seconds);", null)
            _currentPositionMs.value = positionMs
        }
    }

    fun setVolume(vol: Float) {
        val pct = (vol * 100).toInt().coerceIn(0, 100)
        mainHandler.post {
            webView?.evaluateJavascript("javascript:setVolume($pct);", null)
        }
    }

    fun stop() {
        mainHandler.post {
            webView?.evaluateJavascript("javascript:pauseVideo();", null)
            _isPlaying.value = false
            _currentPositionMs.value = 0
            _isBuffering.value = false
        }
    }

    private inner class AndroidYouTubeBridge {
        @JavascriptInterface
        fun postMessage(jsonString: String) {
            try {
                val json = JSONObject(jsonString)
                when (json.optString("type")) {
                    "state" -> {
                        val state = json.optInt("state")
                        // 1: PLAYING, 2: PAUSED, 3: BUFFERING, 0: ENDED, 5: CUED
                        mainHandler.post {
                            when (state) {
                                1 -> {
                                    _isPlaying.value = true
                                    _isBuffering.value = false
                                }
                                2 -> {
                                    _isPlaying.value = false
                                    _isBuffering.value = false
                                }
                                3 -> {
                                    _isBuffering.value = true
                                }
                                0 -> {
                                    _isPlaying.value = false
                                    _isBuffering.value = false
                                    onCompletionListener?.invoke()
                                }
                                5 -> {
                                    _isBuffering.value = false
                                }
                            }
                        }
                    }
                    "time" -> {
                        val currentSec = json.optDouble("current", 0.0)
                        val totalSec = json.optDouble("total", 0.0)
                        mainHandler.post {
                            _currentPositionMs.value = (currentSec * 1000).toInt()
                            if (totalSec > 0) {
                                _durationMs.value = (totalSec * 1000).toInt()
                            }
                        }
                    }
                    "ready" -> {
                        // JS side plays the pending id itself once the YT player is ready.
                        isApiReady = true
                    }
                    "error" -> {
                        val code = json.optInt("code", -1)
                        mainHandler.post {
                            _isBuffering.value = false
                            _isPlaying.value = false
                            onErrorListener?.invoke(code)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getPlayerHtml(): String = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <style>
            * { margin:0; padding:0; box-sizing: border-box; background-color:#000; }
            html, body { width:100%; height:100%; background:#000; overflow:hidden; }
            #player { width:100%; height:100%; position:absolute; top:0; left:0; }
          </style>
        </head>
        <body>
          <div id="player"></div>
          <script>
            var tag = document.createElement('script');
            tag.src = "https://www.youtube.com/iframe_api";
            var firstScriptTag = document.getElementsByTagName('script')[0];
            firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

            var player = null;
            var isReady = false;
            var pendingId = null;
            var progressInterval = null;

            function onYouTubeIframeAPIReady() {
              player = new YT.Player('player', {
                height: '100%',
                width: '100%',
                playerVars: {
                  'autoplay': 1,
                  'playsinline': 1,
                  'controls': 0,
                  'fs': 0,
                  'rel': 0,
                  'modestbranding': 1,
                  'origin': '$embedOrigin',
                  'widget_referrer': '$embedOrigin'
                },
                events: {
                  'onReady': onPlayerReady,
                  'onStateChange': onPlayerStateChange,
                  'onError': onPlayerError
                }
              });
            }

            function onPlayerReady(event) {
              isReady = true;
              if (window.AndroidBridge) {
                window.AndroidBridge.postMessage(JSON.stringify({ type: 'ready' }));
              }
              if (pendingId) {
                playVideoById(pendingId);
                pendingId = null;
              }
              if (progressInterval) clearInterval(progressInterval);
              progressInterval = setInterval(sendProgress, 350);
            }

            function onPlayerStateChange(event) {
              if (window.AndroidBridge) {
                window.AndroidBridge.postMessage(JSON.stringify({
                  type: 'state',
                  state: event.data
                }));
              }
              if (event.data === 1 && !progressInterval) {
                progressInterval = setInterval(sendProgress, 350);
              }
            }

            function onPlayerError(event) {
              if (window.AndroidBridge) {
                window.AndroidBridge.postMessage(JSON.stringify({
                  type: 'error',
                  code: event.data
                }));
              }
            }

            function sendProgress() {
              if (player && typeof player.getCurrentTime === 'function' && window.AndroidBridge) {
                try {
                  var cur = player.getCurrentTime() || 0;
                  var dur = player.getDuration() || 0;
                  window.AndroidBridge.postMessage(JSON.stringify({
                    type: 'time',
                    current: cur,
                    total: dur
                  }));
                } catch(e){}
              }
            }

            function playVideoById(videoId) {
              if (player && typeof player.loadVideoById === 'function') {
                player.loadVideoById({
                  videoId: videoId,
                  startSeconds: 0,
                  suggestedQuality: 'small'
                });
              } else {
                pendingId = videoId;
              }
            }

            function pauseVideo() {
              if (player && typeof player.pauseVideo === 'function') {
                player.pauseVideo();
              }
            }

            function resumeVideo() {
              if (player && typeof player.playVideo === 'function') {
                player.playVideo();
              }
            }

            function seekToTime(seconds) {
              if (player && typeof player.seekTo === 'function') {
                player.seekTo(seconds, true);
              }
            }

            function setVolume(pct) {
              if (player && typeof player.setVolume === 'function') {
                player.setVolume(pct);
              }
            }
          </script>
        </body>
        </html>
    """.trimIndent()
}
