package com.example.data

import com.example.model.Ayah
import com.example.model.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class QuranRepository {

    val surahs: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "The Opener", 7, "Meccan"),
        Surah(2, "البقرة", "Al-Baqarah", "The Cow", 286, "Medinan"),
        Surah(3, "آل عمران", "Ali 'Imran", "Family of Imran", 200, "Medinan"),
        Surah(4, "النساء", "An-Nisa", "The Women", 176, "Medinan"),
        Surah(5, "المائدة", "Al-Ma'idah", "The Table Spread", 120, "Medinan"),
        Surah(6, "الأنعام", "Al-An'am", "The Cattle", 165, "Meccan"),
        Surah(7, "الأعراف", "Al-A'raf", "The Heights", 206, "Meccan"),
        Surah(8, "الأنفال", "Al-Anfal", "The Spoils of War", 75, "Medinan"),
        Surah(9, "التوبة", "At-Tawbah", "The Repentance", 129, "Medinan"),
        Surah(10, "يونس", "Yunus", "Jonah", 109, "Meccan"),
        Surah(11, "هود", "Hud", "Hud", 123, "Meccan"),
        Surah(12, "يوسف", "Yusuf", "Joseph", 111, "Meccan"),
        Surah(13, "الرعد", "Ar-Ra'd", "The Thunder", 43, "Medinan"),
        Surah(14, "إبراهيم", "Ibrahim", "Abraham", 52, "Meccan"),
        Surah(15, "الحجر", "Al-Hijr", "The Rocky Tract", 99, "Meccan"),
        Surah(16, "النحل", "An-Nahl", "The Bee", 128, "Meccan"),
        Surah(17, "الإسراء", "Al-Isra", "The Night Journey", 111, "Meccan"),
        Surah(18, "الكهف", "Al-Kahf", "The Cave", 110, "Meccan"),
        Surah(19, "مريم", "Maryam", "Mary", 98, "Meccan"),
        Surah(20, "طه", "Taha", "Ta-Ha", 135, "Meccan"),
        Surah(21, "الأنبياء", "Al-Anbiya", "The Prophets", 112, "Meccan"),
        Surah(22, "الحج", "Al-Hajj", "The Pilgrimage", 78, "Medinan"),
        Surah(23, "المؤمنون", "Al-Mu'minun", "The Believers", 118, "Meccan"),
        Surah(24, "النور", "An-Nur", "The Light", 64, "Medinan"),
        Surah(25, "الفرقان", "Al-Furqan", "The Criterion", 77, "Meccan"),
        Surah(26, "الشعراء", "Ash-Shu'ara", "The Poets", 227, "Meccan"),
        Surah(27, "النمل", "An-Naml", "The Ant", 93, "Meccan"),
        Surah(28, "القصص", "Al-Qasas", "The Stories", 88, "Meccan"),
        Surah(29, "العنكبوت", "Al-'Ankabut", "The Spider", 69, "Meccan"),
        Surah(30, "الروم", "Ar-Rum", "The Romans", 60, "Meccan"),
        Surah(31, "لقمان", "Luqman", "Luqman", 34, "Meccan"),
        Surah(32, "السجدة", "As-Sajdah", "The Prostration", 30, "Meccan"),
        Surah(33, "الأحزاب", "Al-Ahzab", "The Combined Forces", 73, "Medinan"),
        Surah(34, "سبأ", "Saba", "Sheba", 54, "Meccan"),
        Surah(35, "فاطر", "Fatir", "Originator", 45, "Meccan"),
        Surah(36, "يس", "Ya-Sin", "Ya-Sin", 83, "Meccan"),
        Surah(37, "الصافات", "As-Saffat", "Those Who Set the Ranks", 182, "Meccan"),
        Surah(38, "ص", "Sad", "Sad", 88, "Meccan"),
        Surah(39, "الزمر", "Az-Zumar", "The Troops", 75, "Meccan"),
        Surah(40, "غافر", "Ghafir", "The Forgiver", 85, "Meccan"),
        Surah(41, "فصلت", "Fussilat", "Explained in Detail", 54, "Meccan"),
        Surah(42, "الشورى", "Ash-Shura", "The Consultation", 53, "Meccan"),
        Surah(43, "الزخرف", "Az-Zukhruf", "The Ornaments of Gold", 89, "Meccan"),
        Surah(44, "الدخان", "Ad-Dukhan", "The Smoke", 59, "Meccan"),
        Surah(45, "الجاثية", "Al-Jathiyah", "The Crouching", 37, "Meccan"),
        Surah(46, "الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills", 35, "Meccan"),
        Surah(47, "محمد", "Muhammad", "Muhammad", 38, "Medinan"),
        Surah(48, "الفتح", "Al-Fath", "The Victory", 29, "Medinan"),
        Surah(49, "الحجرات", "Al-Hujurat", "The Rooms", 18, "Medinan"),
        Surah(50, "ق", "Qaf", "Qaf", 45, "Meccan"),
        Surah(51, "الذاريات", "Adh-Dhariyat", "The Winnowing Winds", 60, "Meccan"),
        Surah(52, "الطور", "At-Tur", "The Mount", 49, "Meccan"),
        Surah(53, "النجم", "An-Najm", "The Star", 62, "Meccan"),
        Surah(54, "القمر", "Al-Qamar", "The Moon", 55, "Meccan"),
        Surah(55, "الرحمن", "Ar-Rahman", "The Beneficent", 78, "Medinan"),
        Surah(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", 96, "Meccan"),
        Surah(57, "الحديد", "Al-Hadid", "The Iron", 29, "Medinan"),
        Surah(58, "المجادلة", "Al-Mujadila", "The Pleading Woman", 22, "Medinan"),
        Surah(59, "الحشر", "Al-Hashr", "The Exile", 24, "Medinan"),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "She That Is to Be Examined", 13, "Medinan"),
        Surah(61, "الصف", "As-Saff", "The Ranks", 14, "Medinan"),
        Surah(62, "الجمعة", "Al-Jumu'ah", "The Congregation, Friday", 11, "Medinan"),
        Surah(63, "المنافقون", "Al-Munafiqun", "The Hypocrites", 11, "Medinan"),
        Surah(64, "التغابن", "At-Taghabun", "The Mutual Disillusion", 18, "Medinan"),
        Surah(65, "الطلاق", "At-Talaq", "The Divorce", 12, "Medinan"),
        Surah(66, "التحريم", "At-Tahrim", "The Prohibition", 12, "Medinan"),
        Surah(67, "الملك", "Al-Mulk", "The Sovereignty", 30, "Meccan"),
        Surah(68, "القلم", "Al-Qalam", "The Pen", 52, "Meccan"),
        Surah(69, "الحاقة", "Al-Haqqah", "The Reality", 52, "Meccan"),
        Surah(70, "المعارج", "Al-Ma'arij", "The Ascending Stairways", 44, "Meccan"),
        Surah(71, "نوح", "Nuh", "Noah", 28, "Meccan"),
        Surah(72, "الجن", "Al-Jinn", "The Jinn", 28, "Meccan"),
        Surah(73, "المزمل", "Al-Muzzammil", "The Enshrouded One", 20, "Meccan"),
        Surah(74, "المدثر", "Al-Muddaththir", "The Cloaked One", 56, "Meccan"),
        Surah(75, "القيامة", "Al-Qiyamah", "The Resurrection", 40, "Meccan"),
        Surah(76, "الإنسان", "Al-Insan", "Man", 31, "Medinan"),
        Surah(77, "المرسلات", "Al-Mursalat", "The Emissaries", 50, "Meccan"),
        Surah(78, "النبأ", "An-Naba", "The Tidings", 40, "Meccan"),
        Surah(79, "النازعات", "An-Nazi'at", "Those Who Drag Forth", 46, "Meccan"),
        Surah(80, "عبس", "Abasa", "He Frowned", 42, "Meccan"),
        Surah(81, "التكوير", "At-Takwir", "The Overthrowing", 29, "Meccan"),
        Surah(82, "الانفطار", "Al-Infitar", "The Cleaving", 19, "Meccan"),
        Surah(83, "المطففين", "Al-Mutaffifin", "Defrauding", 36, "Meccan"),
        Surah(84, "الانشقاق", "Al-Inshiqaq", "The Splitting Open", 25, "Meccan"),
        Surah(85, "البروج", "Al-Buruj", "The Mansions of the Stars", 22, "Meccan"),
        Surah(86, "الطارق", "At-Tariq", "The Nightcomer", 17, "Meccan"),
        Surah(87, "الأعلى", "Al-A'la", "The Most High", 19, "Meccan"),
        Surah(88, "الغاشية", "Al-Ghashiyah", "The Overwhelming", 26, "Meccan"),
        Surah(89, "الفجر", "Al-Fajr", "The Dawn", 30, "Meccan"),
        Surah(90, "البلد", "Al-Balad", "The City", 20, "Meccan"),
        Surah(91, "الشمس", "Ash-Shams", "The Sun", 15, "Meccan"),
        Surah(92, "الليل", "Al-Layl", "The Night", 21, "Meccan"),
        Surah(93, "الضحى", "Ad-Duha", "The Morning Hours", 11, "Meccan"),
        Surah(94, "الشرح", "Ash-Sharh", "The Relief", 8, "Meccan"),
        Surah(95, "التين", "At-Tin", "The Fig", 8, "Meccan"),
        Surah(96, "العلق", "Al-'Alaq", "The Clot", 19, "Meccan"),
        Surah(97, "القدر", "Al-Qadr", "The Power, Fate", 5, "Meccan"),
        Surah(98, "البينة", "Al-Bayyinah", "The Clear Proof", 8, "Medinan"),
        Surah(99, "الزلزلة", "Az-Zalzalah", "The Earthquake", 8, "Medinan"),
        Surah(100, "العاديات", "Al-'Adiyat", "The Courser", 11, "Meccan"),
        Surah(101, "القارعة", "Al-Qari'ah", "The Calamity", 11, "Meccan"),
        Surah(102, "التكاثر", "At-Takathur", "Rivalry in World Increase", 8, "Meccan"),
        Surah(103, "العصر", "Al-'Asr", "The Declining Day, Epoch", 3, "Meccan"),
        Surah(104, "الهمزة", "Al-Humazah", "The Traducer", 9, "Meccan"),
        Surah(105, "الفيل", "Al-Fil", "The Elephant", 5, "Meccan"),
        Surah(106, "قريش", "Quraysh", "Quraysh", 4, "Meccan"),
        Surah(107, "الماعون", "Al-Ma'un", "Small Kindnesses", 7, "Meccan"),
        Surah(108, "الكوثر", "Al-Kawthar", "Abundance", 3, "Meccan"),
        Surah(109, "الكافرون", "Al-Kafirun", "The Disbelievers", 6, "Meccan"),
        Surah(110, "النصر", "An-Nasr", "The Divine Support", 3, "Medinan"),
        Surah(111, "المسد", "Al-Masad", "The Palm Fibre", 5, "Meccan"),
        Surah(112, "الإخلاص", "Al-Ikhlas", "Sincerity", 4, "Meccan"),
        Surah(113, "الفلق", "Al-Falaq", "The Daybreak", 5, "Meccan"),
        Surah(114, "الناس", "An-Nas", "Mankind", 6, "Meccan")
    )

    private val offlineSampleSurahs: Map<Int, List<Ayah>> = mapOf(
        1 to listOf(
            Ayah(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful."),
            Ayah(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "[All] praise is [due] to Allah, Lord of the worlds -"),
            Ayah(3, "الرَّحْمَٰنِ الرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,"),
            Ayah(4, "مَالِكِ يَوْمِ الدِّينِ", "Sovereign of the Day of Recompense."),
            Ayah(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help."),
            Ayah(6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path -"),
            Ayah(7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.")
        ),
        108 to listOf(
            Ayah(1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "Indeed, We have granted you, [O Muhammad], al-Kawthar."),
            Ayah(2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "So pray to your Lord and sacrifice [to Him alone]."),
            Ayah(3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "Indeed, your enemy is the one cut off.")
        ),
        112 to listOf(
            Ayah(1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Say, 'He is Allah, [who is] One,"),
            Ayah(2, "اللَّهُ الصَّمَدُ", "Allah, the Eternal Refuge."),
            Ayah(3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,"),
            Ayah(4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "Nor is there to Him any equivalent.'")
        ),
        113 to listOf(
            Ayah(1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Say, 'I seek refuge in the Lord of daybreak"),
            Ayah(2, "مِن شَرِّ مَا خَلَقَ", "From the evil of that which He created"),
            Ayah(3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles"),
            Ayah(4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "And from the evil of the blowers in knots"),
            Ayah(5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.'")
        ),
        114 to listOf(
            Ayah(1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Say, 'I seek refuge in the Lord of mankind,"),
            Ayah(2, "مَلِكِ النَّاسِ", "The Sovereign of mankind."),
            Ayah(3, "إِلَٰهِ النَّاسِ", "The God of mankind,"),
            Ayah(4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "From the evil of the retreating whisperer -"),
            Ayah(5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Who whispers into the breasts of mankind -"),
            Ayah(6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "From among the jinn and mankind.'")
        )
    )

    suspend fun getAyahsForSurah(surahNumber: Int): List<Ayah> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-simple,en.sahih")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.requestMethod = "GET"

                if (conn.responseCode == 200) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    conn.disconnect()

                    val root = JSONObject(resp)
                    val dataArr = root.getJSONArray("data")
                    val arabicAyahs = dataArr.getJSONObject(0).getJSONArray("ayahs")
                    val englishAyahs = dataArr.getJSONObject(1).getJSONArray("ayahs")

                    val result = mutableListOf<Ayah>()
                    val count = minOf(arabicAyahs.length(), englishAyahs.length())
                    for (i in 0 until count) {
                        val arObj = arabicAyahs.getJSONObject(i)
                        val enObj = englishAyahs.getJSONObject(i)
                        result.add(
                            Ayah(
                                numberInSurah = arObj.getInt("numberInSurah"),
                                textAr = arObj.getString("text"),
                                textEn = enObj.getString("text")
                            )
                        )
                    }
                    if (result.isNotEmpty()) return@withContext result
                }
                conn.disconnect()
            } catch (_: Exception) {}

            // Fallback to offline samples or generic placeholder if offline
            offlineSampleSurahs[surahNumber] ?: listOf(
                Ayah(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful."),
                Ayah(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "All praise is due to Allah, Lord of the worlds."),
                Ayah(3, "إِنَّ مَعَ الْعُسْرِ يُسْرًا", "Indeed, with hardship [will be] ease.")
            )
        }
    }
}
