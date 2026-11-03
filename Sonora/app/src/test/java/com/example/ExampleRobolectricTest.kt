package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Track
import com.example.data.repository.MusicRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Sonora", appName)
  }

  @Test
  fun `track formatted duration is correct`() {
    val track = Track(
      id = "test_1",
      title = "Test Song",
      artist = "Test Artist",
      album = "Test Album",
      durationSeconds = 215,
      streamUrl = "https://example.com/audio.mp3",
      coverUrl = "https://example.com/cover.jpg"
    )
    assertEquals("3:35", track.formattedDuration)
  }

  @Test
  fun `smartools download url is properly formed`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = MusicRepository(context)
    val url = repo.getSmartoolsDownloadUrl("1fI48iN0X28")
    assertTrue(url.startsWith("https://smartools.pro/youtube-video-downloader/?url="))
    assertTrue(url.contains("1fI48iN0X28"))
  }
}
