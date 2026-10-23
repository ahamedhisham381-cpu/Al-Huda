package com.example.model

data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

data class HijriDate(
    val day: String,
    val monthAr: String,
    val monthEn: String,
    val year: String
)

data class DailyWisdom(
    val verse: String,
    val translation: String,
    val reflection: String,
    val actionItem: String
)

data class LibraryItem(
    val title: String,
    val arabicContent: String,
    val englishContent: String,
    val source: String,
    val explanation: String
)

data class LibraryContent(
    val quranVerse: LibraryItem,
    val hadith: LibraryItem,
    val story: LibraryItem
)

data class HolyFestival(
    val nameEn: String,
    val nameAr: String,
    val hijriDate: String,
    val gregorianDate: String,
    val significanceEn: String,
    val significanceAr: String,
    val category: String // "Celebration", "Holy Month", "Remembrance"
)

data class Surah(
    val number: Int,
    val nameAr: String,
    val nameEn: String,
    val englishTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String // "Meccan" or "Medinan"
)

data class Ayah(
    val numberInSurah: Int,
    val textAr: String,
    val textEn: String,
    val tafsirText: String = ""
)

data class Reciter(
    val id: String,
    val name: String,
    val subtext: String
)

data class HadithItem(
    val id: Int,
    val collection: String,
    val number: String,
    val narrator: String,
    val arabicText: String,
    val englishText: String,
    val chapter: String,
    val grade: String = "Sahih"
)

data class AllahName(
    val number: Int,
    val arabic: String,
    val transliteration: String,
    val meaning: String,
    val explanation: String,
    val quranReference: String
)

data class DhikrPhrase(
    val arabic: String,
    val english: String,
    val translation: String
)

data class CityLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val countryCode: String = ""
)

/** A resolved place (search result, reverse-geocode result or IP lookup). */
data class GeoPlace(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val countryCode: String = ""
)

data class DuaItem(
    val id: String,
    val category: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val reference: String,
    val virtue: String = ""
)

data class QuizQuestion(
    val id: Int,
    val category: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class ArabicLesson(
    val id: Int,
    val letterOrWord: String,
    val transliteration: String,
    val meaning: String,
    val category: String,
    val exampleQuran: String
)

data class SeerahEvent(
    val yearHijriOrAD: String,
    val title: String,
    val description: String,
    val location: String,
    val keyTakeaway: String
)

data class KidsStory(
    val id: Int,
    val title: String,
    val arabicTitle: String,
    val moral: String,
    val storyText: String,
    val duaText: String
)

data class HajjStep(
    val stepNumber: Int,
    val isUmrah: Boolean,
    val title: String,
    val arabicTitle: String,
    val description: String,
    val duaText: String,
    val tips: String
)

data class GoodDeed(
    val id: String,
    val title: String,
    val arabicTitle: String,
    val category: String,
    val virtue: String,
    val isCompleted: Boolean = false
)

data class FatwaItem(
    val id: Int,
    val category: String,
    val question: String,
    val answerSummary: String,
    val reference: String
)

data class MosqueItem(
    val id: String,
    val name: String,
    val address: String,
    val distanceKm: Double,
    val bearingDegrees: Float,
    val contact: String
)

data class NewsArticle(
    val id: Int,
    val title: String,
    val category: String,
    val date: String,
    val summary: String,
    val readTime: String
)

data class CommunityPost(
    val id: String,
    val authorName: String,
    val country: String,
    val text: String,
    val likesCount: Int = 0,
    val timestamp: String
)

data class AzkarItem(
    val id: String,
    val section: String, // "Morning" or "Evening"
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val repeat: Int,
    val reference: String
)
