package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.EducationAndWorshipRepository
import com.example.data.PrayerRepository
import com.example.data.QuranRepository
import com.example.data.SpiritualRepository
import com.example.util.QiblaHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        assertEquals("Al-Huda", appName)
    }

    @Test
    fun `prayer calculation returns valid times offline`() {
        val repo = PrayerRepository()
        val (times, hijri) = repo.calculateOfflinePrayerTimes(21.4225, 39.8262)
        assertNotNull(times.fajr)
        assertNotNull(times.dhuhr)
        assertNotNull(times.asr)
        assertNotNull(times.maghrib)
        assertNotNull(times.isha)
        assertTrue(times.fajr.contains(":"))
        assertTrue(hijri.day.isNotBlank())
        assertTrue(hijri.year.isNotBlank())
    }

    @Test
    fun `quran repository contains all 114 surahs`() {
        val repo = QuranRepository()
        assertEquals(114, repo.surahs.size)
        assertEquals("Al-Fatihah", repo.surahs[0].nameEn)
        assertEquals("An-Nas", repo.surahs[113].nameEn)
    }

    @Test
    fun `qibla calculation computes valid angle and distance`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val qibla = QiblaHelper(context)
        // From Cairo (30.0444, 31.2357) towards Makkah (21.4225, 39.8262) is approx ~136 degrees SE
        val bearing = qibla.calculateQiblaBearing(30.0444, 31.2357)
        assertTrue(bearing > 120f && bearing < 150f)
        val distKm = qibla.calculateDistanceToKaabaKm(30.0444, 31.2357)
        assertTrue(distKm > 1000 && distKm < 1500)
    }

    @Test
    fun `education and worship repository contains comprehensive 30 features data`() {
        val repo = EducationAndWorshipRepository()
        // Feature 7: 99 Names
        assertEquals(99, repo.namesOfAllah.size)
        assertEquals("الرَّحْمَنُ", repo.namesOfAllah[0].arabic)
        assertEquals("الصَّبُورُ", repo.namesOfAllah[98].arabic)

        // Feature 6: Hadith
        assertTrue(repo.hadiths.isNotEmpty())
        assertTrue(repo.hadiths[0].collection.contains("Bukhari"))

        // Feature 17: Duas
        assertTrue(repo.duas.isNotEmpty())

        // Feature 11: Quiz
        assertTrue(repo.quizQuestions.isNotEmpty())

        // Feature 12: Arabic Lessons
        assertTrue(repo.arabicLessons.isNotEmpty())

        // Feature 13: Seerah
        assertTrue(repo.seerahEvents.isNotEmpty())

        // Feature 19: Hajj & Umrah
        assertTrue(repo.hajjSteps.isNotEmpty())

        // Feature 4: Reciters
        assertEquals(4, repo.reciters.size)
    }

    @Test
    fun `prayer notification scheduler handles scheduling and cancellation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = PrayerRepository()
        val (times, _) = repo.calculateOfflinePrayerTimes(21.4225, 39.8262)

        // Scheduling enabled should execute without crashing
        com.example.util.PrayerNotificationScheduler.scheduleAllPrayerAlerts(
            context,
            times,
            "Makkah",
            enabled = true
        )

        // Cancellation should execute cleanly
        com.example.util.PrayerNotificationScheduler.scheduleAllPrayerAlerts(
            context,
            times,
            "Makkah",
            enabled = false
        )
    }

    @Test
    fun `audio helper executes dhikr bead click and azan helper initializes`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audioHelper = com.example.util.AudioHelper(context)
        audioHelper.playBeadClick()
        audioHelper.playTick()

        val azanHelper = com.example.util.AzanAudioHelper(context)
        assertNotNull(azanHelper)
    }
}
