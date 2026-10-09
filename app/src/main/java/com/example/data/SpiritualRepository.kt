package com.example.data

import com.example.model.DailyWisdom
import com.example.model.DhikrPhrase
import com.example.model.HolyFestival
import com.example.model.LibraryContent
import com.example.model.LibraryItem
import java.util.Calendar

class SpiritualRepository {

    val dhikrPhrases = listOf(
        DhikrPhrase("سُبْحَانَ اللَّهِ", "Subhan Allah", "Glory be to Allah"),
        DhikrPhrase("الْحَمْدُ لِلَّهِ", "Alhamdulillah", "All praise is due to Allah"),
        DhikrPhrase("اللَّهُ أَكْبَرُ", "Allahu Akbar", "Allah is the Greatest"),
        DhikrPhrase("أَسْتَغْفِرُ اللَّهَ", "Astaghfirullah", "I seek forgiveness from Allah"),
        DhikrPhrase("لَا إِلَٰهَ إِلَّا اللَّهُ", "La ilaha illallah", "There is no deity worthy of worship except Allah"),
        DhikrPhrase("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", "Subhan Allahi wa bihamdih", "Glory and praise be to Allah"),
        DhikrPhrase("لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "La hawla wa la quwwata illa billah", "There is no power nor strength except by Allah")
    )

    private val wisdomList = listOf(
        DailyWisdom(
            verse = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            translation = "Verily, in the remembrance of Allah do hearts find rest.",
            reflection = "Constant remembrance of your Creator brings deep tranquility and serenity to a restless soul amidst the chaos of everyday life.",
            actionItem = "Recite SubhanAllah 33 times with full presence of heart after your next prayer."
        ),
        DailyWisdom(
            verse = "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            translation = "Verily, with hardship comes ease.",
            reflection = "Every trial and hardship carries within it multiple avenues of relief, spiritual elevation, and divine mercy. Never lose hope.",
            actionItem = "Be patient with a current difficulty and make sincere du'a for someone going through hardship."
        ),
        DailyWisdom(
            verse = "وَقُل رَّبِّ زِدْنِي عِلْمًا",
            translation = "And say, 'My Lord, increase me in knowledge.'",
            reflection = "Seeking beneficial knowledge is a noble lifelong journey that draws the believer closer to understanding divine wisdom.",
            actionItem = "Read one page of the Quran with contemplation and study its meaning today."
        ),
        DailyWisdom(
            verse = "إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
            translation = "Indeed, Allah is with the patient.",
            reflection = "Patience is not passive waiting; it is active perseverance and remaining steadfast on goodness while trusting Allah's perfect decree.",
            actionItem = "Practice restraint in speech when provoked and respond with gentleness."
        ),
        DailyWisdom(
            verse = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
            translation = "So remember Me; I will remember you. And be grateful to Me and do not deny Me.",
            reflection = "Imagine the honor of being remembered by the Lord of the heavens and the earth merely by uttering His praise.",
            actionItem = "List three hidden blessings in your life and express heartfelt gratitude to Allah in your sujood."
        )
    )

    fun getDailyWisdom(): DailyWisdom {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return wisdomList[dayOfYear % wisdomList.size]
    }

    fun getLibraryContent(): LibraryContent {
        return LibraryContent(
            quranVerse = LibraryItem(
                title = "The Opening (Al-Fatihah)",
                arabicContent = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ • الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
                englishContent = "In the name of Allah, the Entirely Merciful, the Especially Merciful. All praise is due to Allah, Lord of all the worlds.",
                source = "Surah Al-Fatihah 1:1-2",
                explanation = "Every noble deed begins with the divine name, and all genuine gratitude belongs to the Sustainer who nurtures all creation."
            ),
            hadith = LibraryItem(
                title = "Actions are by Intentions",
                arabicContent = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
                englishContent = "Actions are judged solely by intentions, and every person will attain only what they intended.",
                source = "Sahih al-Bukhari 1, Sahih Muslim 1907",
                explanation = "The purity and sincerity of the heart transform ordinary daily habits into rewarded acts of worship and spiritual devotion."
            ),
            story = LibraryItem(
                title = "The Power of Patience in Ta'if",
                arabicContent = "اللَّهُمَّ إِلَيْكَ أَشْكُو ضَعْفَ قُوَّتِي وَقِلَّةَ حِيلَتِي",
                englishContent = "When the Prophet Muhammad (PBUH) faced rejection and severe trials at Ta'if, angel Jibreel offered to crush the city between two mountains. Instead, the Messenger prayed with profound compassion: 'O Allah, forgive my people, for they know not.'",
                source = "Seerah an-Nabawiyyah (Islamic History)",
                explanation = "True spiritual strength lies in mercy over vengeance, forgiveness in moments of hardship, and enduring faith in Allah's ultimate wisdom."
            )
        )
    }

    fun getHolyFestivals(): List<HolyFestival> {
        return listOf(
            HolyFestival(
                nameEn = "Ramadan",
                nameAr = "شهر رمضان المبارك",
                hijriDate = "1st Ramadan",
                gregorianDate = "Expected March / April",
                significanceEn = "The blessed month of fasting from dawn to sunset, deep Quranic recitation, charity, and spiritual renewal.",
                significanceAr = "شهر الصيام والقرآن الكريم والتقرب إلى الله بالأعمال الصالحة والصدقات.",
                category = "Holy Month"
            ),
            HolyFestival(
                nameEn = "Eid al-Fitr",
                nameAr = "عيد الفطر المبارك",
                hijriDate = "1st Shawwal",
                gregorianDate = "Marks end of Ramadan",
                significanceEn = "Joyous holiday of thanksgiving celebrating the completion of the blessed month of Ramadan with communal prayer and gifts.",
                significanceAr = "يوم الجائزة والفرح بإتمام صيام شهر رمضان المبارك وصلة الأرحام وتوزيع زكاة الفطر.",
                category = "Celebration"
            ),
            HolyFestival(
                nameEn = "Day of Arafah",
                nameAr = "يوم عرفة",
                hijriDate = "9th Dhu al-Hijjah",
                gregorianDate = "Pinnacle of Hajj",
                significanceEn = "The greatest day of Hajj pilgrimage where millions stand in prayer; fasting on this day expiates sins of two years.",
                significanceAr = "أعظم أيام الحج وصيامه لغير الحاج يكفر ذنوب سنة ماضية وسنة باقية.",
                category = "Remembrance"
            ),
            HolyFestival(
                nameEn = "Eid al-Adha",
                nameAr = "عيد الأضحى المبارك",
                hijriDate = "10th Dhu al-Hijjah",
                gregorianDate = "Festival of Sacrifice",
                significanceEn = "The Feast of Sacrifice honoring Prophet Ibrahim's unwavering faith and submission to Allah, marked by Udhiyah charity.",
                significanceAr = "عيد النحر وإحياء سنة الخليل إبراهيم عليه السلام والتقرب إلى الله بالأضاحي.",
                category = "Celebration"
            ),
            HolyFestival(
                nameEn = "Islamic New Year",
                nameAr = "رأس السنة الهجرية",
                hijriDate = "1st Muharram",
                gregorianDate = "Commencement of Hijri Year",
                significanceEn = "Marks the historic Hijrah (migration) of the Prophet (PBUH) from Makkah to Madinah, inaugurating the Islamic calendar.",
                significanceAr = "ذكرى هجرة النبي صلى الله عليه وسلم وبداية العام الهجري المبارك.",
                category = "Remembrance"
            ),
            HolyFestival(
                nameEn = "Day of Ashura",
                nameAr = "يوم عاشوراء",
                hijriDate = "10th Muharram",
                gregorianDate = "Sacred Day of Fasting",
                significanceEn = "The day Allah delivered Prophet Musa (Moses) and the Children of Israel from Pharaoh. Recommended to fast.",
                significanceAr = "يوم نجّى الله فيه موسى عليه السلام وبني إسرائيل من فرعون، ويستحب صيامه.",
                category = "Remembrance"
            ),
            HolyFestival(
                nameEn = "Laylat al-Qadr",
                nameAr = "ليلة القدر",
                hijriDate = "Odd nights of late Ramadan",
                gregorianDate = "Night of Decree",
                significanceEn = "The Night of Power, better than a thousand months, in which the Holy Quran was first revealed to humanity.",
                significanceAr = "ليلة القدر خير من ألف شهر تنزل الملائكة والروح فيها بإذن ربهم من كل أمر.",
                category = "Holy Month"
            )
        )
    }
}
