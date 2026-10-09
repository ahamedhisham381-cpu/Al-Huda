package com.example.data

import com.example.model.*

class EducationAndWorshipRepository {

    // 4 Qaris for Quran Audio (Feature 4)
    val reciters = listOf(
        Reciter("mishary", "Mishary Rashid Alafasy", "الشيخ مشاري راشد العفاسي • Kuwait"),
        Reciter("abdulbasit", "Abdul Basit Abdul Samad", "الشيخ عبد الباسط عبد الصمد • Egypt"),
        Reciter("alhusary", "Mahmoud Khalil Al-Husary", "الشيخ محمود خليل الحصري • Murattal"),
        Reciter("alghamdi", "Saad Al-Ghamdi", "الشيخ سعد الغامدي • Saudi Arabia")
    )

    fun getAudioUrlForSurah(reciterId: String, surahNum: Int): String {
        val padded = surahNum.toString().padStart(3, '0')
        return when (reciterId) {
            "abdulbasit" -> "https://server7.mp3quran.net/basit/$padded.mp3"
            "alhusary" -> "https://server13.mp3quran.net/husr/$padded.mp3"
            "alghamdi" -> "https://server7.mp3quran.net/s_gmd/$padded.mp3"
            else -> "https://server8.mp3quran.net/afs/$padded.mp3" // Default Mishary
        }
    }

    // 99 Names of Allah (Feature 7)
    val namesOfAllah: List<AllahName> = listOf(
        AllahName(1, "الرَّحْمَنُ", "Ar-Rahman", "The Most Gracious", "He who wills all goodness, mercy, and compassion for all creation.", "Quran 55:1"),
        AllahName(2, "الرَّحِيمُ", "Ar-Raheem", "The Most Merciful", "He who acts with extreme kindness and grants specialized mercy to believers.", "Quran 1:3"),
        AllahName(3, "الْمَلِكُ", "Al-Malik", "The Sovereign King", "The absolute ruler and supreme master of the entire universe.", "Quran 59:23"),
        AllahName(4, "الْقُدُّوسُ", "Al-Quddus", "The Most Holy", "The Pure, free from all imperfections, deficiencies, and blemishes.", "Quran 59:23"),
        AllahName(5, "السَّلَامُ", "As-Salam", "The Source of Peace", "The giver of safety, serenity, and absolute tranquility.", "Quran 59:23"),
        AllahName(6, "الْمُؤْمِنُ", "Al-Mu'min", "The Guardian of Faith", "He who grants security, safety, and confirms the truth of His prophets.", "Quran 59:23"),
        AllahName(7, "الْمُهَيْمِنُ", "Al-Muhaymin", "The Protector", "The vigilant guardian who oversees and protects all of creation.", "Quran 59:23"),
        AllahName(8, "الْعَزِيزُ", "Al-Aziz", "The Almighty", "The invincible, all-powerful whose might cannot be overcome.", "Quran 59:23"),
        AllahName(9, "الْجَبَّارُ", "Al-Jabbar", "The Compeller", "The restorer who mends broken hearts and whose will prevails.", "Quran 59:23"),
        AllahName(10, "الْمُتَكَبِّرُ", "Al-Mutakabbir", "The Supreme", "The One who is supreme in majesty and transcendence above all.", "Quran 59:23"),
        AllahName(11, "الْخَالِقُ", "Al-Khaliq", "The Creator", "The One who designs and brings everything into existence from nothing.", "Quran 59:24"),
        AllahName(12, "الْبَارِئُ", "Al-Bari'", "The Originator", "The Maker who brings creation into being in perfect harmony and form.", "Quran 59:24"),
        AllahName(13, "الْمُصَوِّرُ", "Al-Musawwir", "The Fashioner", "The Flawless shaper who gives unique forms to each creation.", "Quran 59:24"),
        AllahName(14, "الْغَفَّارُ", "Al-Ghaffar", "The All-Forgiving", "He who repeatedly forgives sins and veils our shortcomings.", "Quran 71:10"),
        AllahName(15, "الْقَهَّارُ", "Al-Qahhar", "The Subduer", "The One who has absolute authority and dominance over all things.", "Quran 13:16"),
        AllahName(16, "الْوَهَّابُ", "Al-Wahhab", "The Bestower", "The continuous giver of blessings without expecting any return.", "Quran 3:8"),
        AllahName(17, "الرَّزَّاقُ", "Ar-Razzaq", "The Provider", "The sustainer who guarantees sustenance for all living beings.", "Quran 51:58"),
        AllahName(18, "الْفَتَّاحُ", "Al-Fattah", "The Opener", "The judge who opens doors of mercy, relief, and divine victory.", "Quran 34:26"),
        AllahName(19, "الْعَلِيمُ", "Al-Aleem", "The All-Knowing", "He whose comprehensive knowledge encompasses everything, seen and unseen.", "Quran 2:158"),
        AllahName(20, "الْقَابِضُ", "Al-Qabid", "The Withholder", "The One who restricts or withholds provision with perfect wisdom.", "Quran 2:245"),
        AllahName(21, "الْبَاسِطُ", "Al-Basit", "The Extender", "The One who expands provision, joy, and mercy for His servants.", "Quran 2:245"),
        AllahName(22, "الْخَافِضُ", "Al-Khafid", "The Abaser", "He who humbles the arrogant and unjust.", "Hadith Tirmidhi"),
        AllahName(23, "الرَّافِعُ", "Ar-Rafi'", "The Exalter", "He who elevates the humble, truthful, and pious believers.", "Quran 58:11"),
        AllahName(24, "الْمُعِزُّ", "Al-Mu'izz", "The Bestower of Honor", "The One who gives true dignity, honor, and victory.", "Quran 3:26"),
        AllahName(25, "الْمُذِلُّ", "Al-Muthill", "The Humiliator", "The One who lowers those who turn away in pride and disobedience.", "Quran 3:26"),
        AllahName(26, "السَّمِيعُ", "As-Samee'", "The All-Hearing", "He who hears all sounds, whispers, and heartfelt prayers.", "Quran 2:127"),
        AllahName(27, "الْبَصِيرُ", "Al-Baseer", "The All-Seeing", "He who sees everything in the heavens and the earth, even in darkness.", "Quran 42:11"),
        AllahName(28, "الْحَكَمُ", "Al-Hakam", "The Judge", "The ultimate arbiter whose judgment is entirely just and final.", "Quran 6:114"),
        AllahName(29, "الْعَدْلُ", "Al-Adl", "The Utterly Just", "The embodiment of perfect justice without a trace of unfairness.", "Quran 6:115"),
        AllahName(30, "اللَّطِيفُ", "Al-Lateef", "The Subtle & Kind", "The subtly benevolent One whose kindness works in unseen ways.", "Quran 6:103"),
        AllahName(31, "الْخَبِيرُ", "Al-Khabeer", "The All-Aware", "The One acquainted with the inner reality of all things.", "Quran 6:18"),
        AllahName(32, "الْحَلِيمُ", "Al-Haleem", "The Most Forbearing", "He who delays punishment and gives ample opportunity to repent.", "Quran 2:225"),
        AllahName(33, "الْعَظِيمُ", "Al-Azeem", "The Magnificent", "The Tremendous One whose greatness is beyond human comprehension.", "Quran 2:255"),
        AllahName(34, "الْغَفُورُ", "Al-Ghafoor", "The Great Forgiver", "The All-Forgiving who completely pardons our major sins.", "Quran 2:173"),
        AllahName(35, "الشَّكُورُ", "Ash-Shakoor", "The Appreciative", "He who rewards immensely for even the smallest good deeds.", "Quran 35:30"),
        AllahName(36, "الْعَلِيُّ", "Al-Aliyy", "The Most High", "The exalted One above all creation in rank, power, and majesty.", "Quran 2:255"),
        AllahName(37, "الْكَبِيرُ", "Al-Kabeer", "The Most Great", "The One whose grandeur surpasses everything that exists.", "Quran 13:9"),
        AllahName(38, "الْحَفِيظُ", "Al-Hafidh", "The Preserver", "The guardian who preserves and protects creation and all deeds.", "Quran 11:57"),
        AllahName(39, "الْمُقِيتُ", "Al-Muqeet", "The Sustainer", "The One who provides nourishment and nourishment to all creation.", "Quran 4:85"),
        AllahName(40, "الْحَسِيبُ", "Al-Haseeb", "The Reckoner", "The One who suffices for all needs and calls all to account.", "Quran 4:6"),
        AllahName(41, "الْجَلِيلُ", "Al-Jaleel", "The Majestic", "The sublime One possessed of supreme dignity and splendor.", "Quran 55:27"),
        AllahName(42, "الْكَرِيمُ", "Al-Kareem", "The Most Bountiful", "The Generous whose gifts never cease and are given freely.", "Quran 27:40"),
        AllahName(43, "الرَّقِيبُ", "Ar-Raqeeb", "The Watchful", "The all-observing guardian from whom nothing is hidden.", "Quran 4:1"),
        AllahName(44, "الْمُجِيبُ", "Al-Mujeeb", "The Responsive", "The One who answers the prayers and cries of those who call upon Him.", "Quran 11:61"),
        AllahName(45, "الْوَاسِعُ", "Al-Wasi'", "The All-Encompassing", "The Boundless in His capacity, knowledge, mercy, and provision.", "Quran 2:268"),
        AllahName(46, "الْحَكِيمُ", "Al-Hakeem", "The All-Wise", "He whose actions, decrees, and laws are endowed with infinite wisdom.", "Quran 2:129"),
        AllahName(47, "الْوَدُودُ", "Al-Wadood", "The Loving One", "The Most Loving who loves His righteous servants with pure affection.", "Quran 11:90"),
        AllahName(48, "الْمَجِيدُ", "Al-Majeed", "The Most Glorious", "The One possessing utmost honor, dignity, and high status.", "Quran 11:73"),
        AllahName(49, "الْبَاعِثُ", "Al-Ba'ith", "The Resurrector", "The One who raises the dead from their graves on the Day of Judgment.", "Quran 22:7"),
        AllahName(50, "الشَّهِيدُ", "Ash-Shaheed", "The Witness", "The One who witnesses every single action and intention.", "Quran 4:79"),
        AllahName(51, "الْحَقُّ", "Al-Haqq", "The Absolute Truth", "The One whose existence, words, and promise are undeniably true.", "Quran 22:6"),
        AllahName(52, "الْوَكِيلُ", "Al-Wakeel", "The Trustee", "The ultimate advocate and reliable protector in whom we place trust.", "Quran 3:173"),
        AllahName(53, "الْقَوِيُّ", "Al-Qawiyy", "The All-Strong", "The One with inexhaustible power and omnipotence.", "Quran 22:40"),
        AllahName(54, "الْمَتِينُ", "Al-Mateen", "The Steadfast", "The Firm One whose strength never wavers nor diminishes.", "Quran 51:58"),
        AllahName(55, "الْوَلِيُّ", "Al-Waliyy", "The Protecting Friend", "The caring ally and guardian of the faithful.", "Quran 4:45"),
        AllahName(56, "الْحَمِيدُ", "Al-Hameed", "The Praiseworthy", "The One uniquely worthy of all praise and veneration.", "Quran 14:1"),
        AllahName(57, "الْمُحْصِي", "Al-Muhsee", "The Counter of All", "The One who records and knows the count of every particle.", "Quran 72:28"),
        AllahName(58, "الْمُبْدِئُ", "Al-Mubdi'", "The Originator", "The One who initiated creation without any prior model.", "Quran 10:4"),
        AllahName(59, "الْمُعِيدُ", "Al-Mu'eed", "The Restorer", "The One who will recreate all beings after their death.", "Quran 10:4"),
        AllahName(60, "الْمُحْيِي", "Al-Muhyee", "The Giver of Life", "The One who grants life to what was lifeless.", "Quran 30:50"),
        AllahName(61, "الْمُمِيتُ", "Al-Mumeet", "The Bringer of Death", "The One who decrees death for all living creatures.", "Quran 3:156"),
        AllahName(62, "الْحَيُّ", "Al-Hayy", "The Ever-Living", "The One who is eternally alive, knowing neither slumber nor death.", "Quran 2:255"),
        AllahName(63, "الْقَيُّومُ", "Al-Qayyoom", "The Self-Sustaining", "The Sustainer upon whom all existence depends.", "Quran 2:255"),
        AllahName(64, "الْوَاجِدُ", "Al-Wajid", "The Finder", "The Resourceful who finds whatever He desires.", "Quran 93:6"),
        AllahName(65, "الْمَاجِدُ", "Al-Majid", "The Noble", "The Magnificent whose nobility and dignity are boundless.", "Quran 11:73"),
        AllahName(66, "الْوَاحِدُ", "Al-Wahid", "The Unique One", "The Indivisible One with no partner in His essence or attributes.", "Quran 2:163"),
        AllahName(67, "الْأَحَدُ", "Al-Ahad", "The Only One", "The Sole, absolute Unity who has no equal or peer.", "Quran 112:1"),
        AllahName(68, "الصَّمَدُ", "As-Samad", "The Eternal Refuge", "The Self-Sufficient upon whom all creation depends for their needs.", "Quran 112:2"),
        AllahName(69, "الْقَادِرُ", "Al-Qadir", "The Omnipotent", "The One who possesses absolute power to do whatever He wills.", "Quran 6:65"),
        AllahName(70, "الْمُقْتَدِرُ", "Al-Muqtadir", "The Determiner", "The Supreme Determiner whose power shapes all events.", "Quran 18:45"),
        AllahName(71, "الْمُقَدِّمُ", "Al-Muqaddim", "The Advancer", "The One who brings forward whatever He wills according to wisdom.", "Quran 50:28"),
        AllahName(72, "الْمُؤَخِّرُ", "Al-Mu'akhkhir", "The Delayer", "The One who postpones events until their appointed time.", "Quran 71:4"),
        AllahName(73, "الْأَوَّلُ", "Al-Awwal", "The First", "The First with no beginning, existing prior to all creation.", "Quran 57:3"),
        AllahName(74, "الْآخِرُ", "Al-Akhir", "The Last", "The Eternal who remains after all else has perished.", "Quran 57:3"),
        AllahName(75, "الظَّاهِرُ", "Az-Zahir", "The Manifest", "The Evident whose existence is proven by His wondrous creation.", "Quran 57:3"),
        AllahName(76, "الْبَاطِنُ", "Al-Batin", "The Hidden", "The Unseen who cannot be grasped by physical eyes in this world.", "Quran 57:3"),
        AllahName(77, "الْوَالِي", "Al-Wali", "The Supreme Governor", "The Ruler who manages the affairs of all existing worlds.", "Quran 13:11"),
        AllahName(78, "الْمُتَعَالِي", "Al-Muta'ali", "The Most Exalted", "The Supremely Exalted far above the attributes of creation.", "Quran 13:9"),
        AllahName(79, "الْبَرُّ", "Al-Barr", "The Most Kind", "The Fountainhead of all righteousness, kindness, and goodness.", "Quran 52:28"),
        AllahName(80, "التَّوَّابُ", "At-Tawwab", "The Relenting Accepter", "The One who continuously turns in forgiveness toward repenting souls.", "Quran 2:37"),
        AllahName(81, "الْمُنْتَقِمُ", "Al-Muntaqim", "The Retributor", "The One who rightfully punishes the tyrannical and unrepentant.", "Quran 32:22"),
        AllahName(82, "الْعَفُوُّ", "Al-Afuww", "The Pardoner", "The One who completely erases sins as if they never happened.", "Quran 4:99"),
        AllahName(83, "الرَّءُوفُ", "Ar-Ra'oof", "The Most Compassionate", "The Tender One filled with deep pity and mercy for His servants.", "Quran 2:207"),
        AllahName(84, "مَالِكُ الْمُلْكِ", "Malik-ul-Mulk", "Owner of All Dominion", "The Sovereign Lord of the kingdom of all heavens and earth.", "Quran 3:26"),
        AllahName(85, "ذُو الْجَلَالِ وَالْإِكْرَامِ", "Thul-Jalali wal-Ikram", "Lord of Glory & Honor", "The Possessor of absolute majesty, reverence, and generosity.", "Quran 55:27"),
        AllahName(86, "الْمُقْسِطُ", "Al-Muqsit", "The Equitable", "The Upholder of justice who judges with absolute fairness.", "Quran 7:29"),
        AllahName(87, "الْجَامِعُ", "Al-Jami'", "The Gatherer", "The One who gathers all humanity on the Day of Resurrection.", "Quran 3:9"),
        AllahName(88, "الْغَنِيُّ", "Al-Ghaniyy", "The Self-Sufficient", "The Rich who is entirely free of any need from any creation.", "Quran 2:267"),
        AllahName(89, "الْمُغْنِي", "Al-Mughni", "The Enricher", "The One who satisfies needs and enriches whom He wills.", "Quran 9:28"),
        AllahName(90, "الْمَانِعُ", "Al-Mani'", "The Preventer", "The One who withholds harm and prevents according to His plan.", "Hadith Tirmidhi"),
        AllahName(91, "الضَّارُّ", "Ad-Darr", "The Afflicter", "The Creator of harm, permitting it for trial, purification, and wisdom.", "Quran 6:17"),
        AllahName(92, "النَّافِعُ", "An-Nafi'", "The Benefactor", "The Creator of all benefit, blessing, and good.", "Quran 30:37"),
        AllahName(93, "النُّورُ", "An-Nur", "The Divine Light", "The Light that illuminates the heavens, earth, and believers' hearts.", "Quran 24:35"),
        AllahName(94, "الْهَادِي", "Al-Hadi", "The Guide", "The Supreme Guide who leads souls to the straight path of salvation.", "Quran 22:54"),
        AllahName(95, "الْبَدِيعُ", "Al-Badee'", "The Incomparable Originator", "The Originator who creates with marvelous, unmatched originality.", "Quran 2:117"),
        AllahName(96, "الْبَاقِي", "Al-Baqi", "The Everlasting", "The Eternal One whose existence will never come to an end.", "Quran 55:27"),
        AllahName(97, "الْوَارِثُ", "Al-Warith", "The Supreme Inheritor", "The One to whom all possessions return after all has ceased.", "Quran 15:23"),
        AllahName(98, "الرَّشِيدُ", "Ar-Rasheed", "The Righteous Guide", "The Infallible Director whose decrees guide to righteous ends.", "Quran 18:17"),
        AllahName(99, "الصَّبُورُ", "As-Saboor", "The Most Patient", "The One who does not rush to punish, bearing all with boundless patience.", "Hadith Bukhari")
    )

    // Hadith Library (Feature 6)
    val hadiths = listOf(
        HadithItem(
            id = 1,
            collection = "Sahih al-Bukhari",
            number = "Hadith 1",
            narrator = "Umar ibn al-Khattab (RA)",
            arabicText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى، فَمَنْ كَانَتْ هِجْرَتُهُ إِلَى دُنْيَا يُصِيبُهَا أَوْ إِلَى امْرَأَةٍ يَنْكِحُهَا، فَهِجْرَتُهُ إِلَى مَا هَاجَرَ إِلَيْهِ.",
            englishText = "Actions are only by intentions, and every man shall have only what he intended. So whose migration was to Allah and His Messenger, his migration is to Allah and His Messenger.",
            chapter = "Revelation (Bad' al-Wahy)"
        ),
        HadithItem(
            id = 2,
            collection = "Sahih Muslim",
            number = "Hadith 223",
            narrator = "Abu Malik al-Ash'ari (RA)",
            arabicText = "الطُّهُورُ شَطْرُ الإِيمَانِ، وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ، وَسُبْحَانَ اللَّهِ وَالْحَمْدُ لِلَّهِ تَمْلآنِ - أَوْ تَمْلأُ - مَا بَيْنَ السَّمَاوَاتِ وَالأَرْضِ.",
            englishText = "Cleanliness is half of faith, and Alhamdulillah (praise be to Allah) fills the scale, and SubhanAllah and Alhamdulillah fill what is between heaven and earth.",
            chapter = "The Book of Purification (Kitab al-Taharah)"
        ),
        HadithItem(
            id = 3,
            collection = "Sunan Abi Dawud",
            number = "Hadith 4941",
            narrator = "Abdullah ibn Amr (RA)",
            arabicText = "الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ، ارْحَمُوا مَنْ فِي الأَرْضِ يَرْحَمْكُمْ مَنْ فِي السَّمَاءِ.",
            englishText = "The merciful will be shown mercy by the Most Merciful. Show mercy to those on the earth, and the One in heaven will show mercy to you.",
            chapter = "General Manners (Kitab al-Adab)"
        ),
        HadithItem(
            id = 4,
            collection = "40 Hadith Nawawi",
            number = "Hadith 13",
            narrator = "Anas ibn Malik (RA)",
            arabicText = "لا يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ.",
            englishText = "None of you truly believes until he loves for his brother what he loves for himself.",
            chapter = "Faith & Compassion"
        ),
        HadithItem(
            id = 5,
            collection = "Riyad as-Salihin",
            number = "Hadith 55",
            narrator = "Abu Huraira (RA)",
            arabicText = "مَنْ سَلَكَ طَرِيقاً يَلْتَمِسُ فِيهِ عِلْماً، سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقاً إِلَى الْجَنَّةِ.",
            englishText = "Whoever follows a path seeking knowledge, Allah will facilitate for him a path to Paradise.",
            chapter = "Virtue of Knowledge"
        )
    )

    // Dua Collection (Feature 17)
    val duas = listOf(
        DuaItem(
            id = "morning_1",
            category = "Morning & Evening",
            title = "Morning Gratitude & Affirmation",
            arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ.",
            transliteration = "Asbahna wa asbahal-mulku lillah, wal-hamdulillahi, la ilaha illallahu wahdahu la shareeka lah.",
            translation = "We have entered a new morning, and dominion belongs to Allah. All praise is for Allah. None has the right to be worshipped except Allah alone without partner.",
            reference = "Hisn al-Muslim 75 (Sahih Muslim)",
            virtue = "Said at the start of every day for protection and gratitude."
        ),
        DuaItem(
            id = "eating_1",
            category = "Food & Drink",
            title = "Before Eating",
            arabic = "بِسْمِ اللَّهِ وَعَلَى بَرَكَةِ اللَّهِ",
            transliteration = "Bismillahi wa 'ala barakatillah",
            translation = "In the name of Allah and upon the blessing of Allah.",
            reference = "Sunan Abi Dawud",
            virtue = "Brings barakah (divine blessing) to sustenance."
        ),
        DuaItem(
            id = "travel_1",
            category = "Travel",
            title = "Supplication for Journey & Travel",
            arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ.",
            transliteration = "Subhanalladhi sakh-khara lana hadha wa ma kunna lahu muqrineen, wa inna ila Rabbina lamunqaliboon.",
            translation = "Glory to Him who has brought this under our control, for we could not have accomplished it ourselves. And indeed, unto our Lord we will return.",
            reference = "Quran 43:13-14",
            virtue = "Sunnah upon boarding vehicle or beginning a journey."
        ),
        DuaItem(
            id = "anxiety_1",
            category = "Forgiveness & Relief",
            title = "Relief from Grief & Debt",
            arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْجُبْنِ وَالْبُخْلِ، وَغَلَبَةِ الدَّيْنِ وَقَهْرِ الرِّجَالِ.",
            transliteration = "Allahumma inni a'oodhu bika minal-hammi wal-hazan, wal-'ajzi wal-kasal, wal-jubni wal-bukhl, wa ghalabatid-dayni wa qahrir-rijal.",
            translation = "O Allah, I seek refuge in You from grief and sadness, from weakness and laziness, from cowardice and stinginess, and from the burden of debt and the oppression of men.",
            reference = "Sahih al-Bukhari 2893",
            virtue = "The Prophet's ﷺ profound prayer for emotional and financial peace."
        ),
        DuaItem(
            id = "forgive_1",
            category = "Forgiveness & Relief",
            title = "Sayyid al-Istighfar (The Master of Forgiveness)",
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ.",
            transliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'oodhu bika min sharri ma sana'tu, aboo'u laka bini'matika 'alayya, wa aboo'u bidhanbi faghfir li, fa-innahu la yaghfirudh-dhunooba illa Anta.",
            translation = "O Allah, You are my Lord; none has the right to be worshipped except You. You created me and I am Your slave. I abide by Your covenant as best as I can. I acknowledge Your favors upon me and confess my sins. Forgive me, for none forgives sins except You.",
            reference = "Sahih al-Bukhari 6306",
            virtue = "Whoever recites this with conviction during the day/night and dies, enters Jannah."
        )
    )

    // Islamic Quiz (Feature 11)
    val quizQuestions = listOf(
        QuizQuestion(
            id = 1,
            category = "Quran",
            question = "Which Surah in the Holy Quran is known as the 'Heart of the Quran'?",
            options = listOf("Surah Al-Baqarah", "Surah Ya-Sin", "Surah Ar-Rahman", "Surah Al-Mulk"),
            correctIndex = 1,
            explanation = "Prophet Muhammad ﷺ said that Surah Ya-Sin is the heart of the Quran (reported in Sunan al-Tirmidhi)."
        ),
        QuizQuestion(
            id = 2,
            category = "Seerah",
            question = "In which cave did the Prophet Muhammad ﷺ receive the very first revelation of the Quran?",
            options = listOf("Cave of Thawr", "Cave of Hira", "Cave of Uhud", "Cave of Safa"),
            correctIndex = 1,
            explanation = "The first revelation (Surah Al-Alaq, verses 1-5) was revealed to the Prophet ﷺ in Cave Hira on Mount Nur."
        ),
        QuizQuestion(
            id = 3,
            category = "Fiqh & Pillars",
            question = "Which of the following is the 3rd pillar of Islam?",
            options = listOf("Salah (Prayer)", "Zakah (Almsgiving)", "Sawm (Fasting)", "Hajj (Pilgrimage)"),
            correctIndex = 1,
            explanation = "The 5 pillars are: Shahadah (Faith), Salah (Prayer), Zakah (Almsgiving), Sawm (Fasting), and Hajj."
        ),
        QuizQuestion(
            id = 4,
            category = "Prophets",
            question = "Which Prophet was swallowed by a giant fish/whale and made sincere repentance?",
            options = listOf("Prophet Yunus (Jonah)", "Prophet Musa (Moses)", "Prophet Ayyub (Job)", "Prophet Ibrahim (Abraham)"),
            correctIndex = 0,
            explanation = "Prophet Yunus (AS) called upon Allah from the belly of the whale saying: 'La ilaha illa Anta, Subhanaka inni kuntu minaz-zalimeen'."
        ),
        QuizQuestion(
            id = 5,
            category = "Quran",
            question = "What is the longest Surah in the Holy Quran?",
            options = listOf("Surah Ali 'Imran", "Surah An-Nisa", "Surah Al-Baqarah", "Surah Al-A'raf"),
            correctIndex = 2,
            explanation = "Surah Al-Baqarah (The Cow) is the longest Surah with 286 verses."
        )
    )

    // Learn Arabic (Feature 12)
    val arabicLessons = listOf(
        ArabicLesson(1, "أ", "Alif", "Letter A", "Alphabet", "إِنَّا أَعْطَيْنَاكَ (Innā)"),
        ArabicLesson(2, "ب", "Baa", "Letter B", "Alphabet", "بِسْمِ اللَّهِ (Bismillāh)"),
        ArabicLesson(3, "ت", "Taa", "Letter T", "Alphabet", "تَبَارَكَ الَّذِي (Tabārak)"),
        ArabicLesson(4, "ر - ح - م", "R-H-M", "Mercy / Compassion (Rahmah)", "Roots", "الرَّحْمَٰنِ الرَّحِيمِ"),
        ArabicLesson(5, "ع - ل - م", "A-L-M", "Knowledge / Awareness ('Ilm)", "Roots", "عَلَّمَ الْإِنسَانَ مَا لَمْ يَعْلَمْ"),
        ArabicLesson(6, "ص - ل - و", "S-L-W", "Prayer / Connection (Salah)", "Roots", "وَأَقِيمُوا الصَّلَاةَ"),
        ArabicLesson(7, "ك - ت - ب", "K-T-B", "Writing / Book (Kitab)", "Roots", "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ فِيهِ"),
        ArabicLesson(8, "ش - ك - ر", "Sh-K-R", "Gratitude / Thanks (Shukr)", "Roots", "لَئِن شَكَرْتُمْ لَأَزِيدَنَّكُمْ")
    )

    // Seerah Timeline (Feature 13)
    val seerahEvents = listOf(
        SeerahEvent("570 CE", "Birth of the Prophet ﷺ", "Born in Makkah in the Year of the Elephant to Aminah and Abdullah.", "Makkah", "The arrival of the final Mercy to mankind."),
        SeerahEvent("595 CE", "Marriage to Khadijah (RA)", "Known as As-Sadiq (The Truthful) and Al-Amin (The Trustworthy), married Khadijah.", "Makkah", "Exemplary partnership of loyalty and mutual support."),
        SeerahEvent("610 CE (40 yrs)", "The First Revelation", "Archangel Jibreel brought the command 'Iqra!' (Read!) at Cave Hira.", "Mount Hira, Makkah", "Beginning of prophethood and Quranic revelation."),
        SeerahEvent("619 CE", "Year of Sorrow & Al-Isra wal-Mi'raj", "Passing of Khadijah and Abu Talib, followed by the miraculous Night Journey and 5 daily prayers gift.", "Jerusalem & Heavens", "Salah instituted as the direct link between believer and Allah."),
        SeerahEvent("622 CE (1 AH)", "The Great Hijrah to Madinah", "Migration from persecution in Makkah to establish the first Muslim community in Madinah.", "Madinah", "Founding of the Islamic calendar and brotherhood (Mu'akhah)."),
        SeerahEvent("624 CE (2 AH)", "The Battle of Badr", "Decisive victory for the Muslims against great odds during Ramadan.", "Badr", "Confirmation of divine help for the steadfast."),
        SeerahEvent("628 CE (6 AH)", "Treaty of Hudaybiyyah", "Pact of peace with the Quraish that opened hearts and led to massive acceptance of Islam.", "Hudaybiyyah", "Called 'a clear victory' (Fathan Mubeena) by Allah."),
        SeerahEvent("630 CE (8 AH)", "Conquest of Makkah", "Peaceful return to Makkah, cleansing the Kaaba of idols, and granting universal pardon.", "Makkah", "Ultimate triumph of mercy, humility, and justice."),
        SeerahEvent("632 CE (10 AH)", "Farewell Pilgrimage & Passing", "The final sermon declaring human equality, rights, and the completion of Islam.", "Mount Arafat", "'Today I have perfected for you your religion.'")
    )

    // Kids Corner (Feature 14)
    val kidsStories = listOf(
        KidsStory(
            id = 1,
            title = "Prophet Yunus & the Whale",
            arabicTitle = "قصة يونس عليه السلام والحوت",
            moral = "Never give up hope and always turn to Allah when you make a mistake.",
            storyText = "Prophet Yunus was inside the belly of a great whale in the dark ocean. He made a heartfelt prayer admitting his fault, and Allah lovingly commanded the whale to gently put him on the warm sandy beach!",
            duaText = "لَا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ"
        ),
        KidsStory(
            id = 2,
            title = "The Honest Little Shepherd",
            arabicTitle = "الراعي الصغير الأمين",
            moral = "Allah sees us everywhere, even when nobody else is watching.",
            storyText = "A traveler offered a boy gold to sell a sheep that wasn't his, saying 'tell the owner a wolf ate it!' The boy smiled and asked: 'Then where is Allah?' The traveler cried tears of joy at his honesty.",
            duaText = "اللَّهُمَّ اهْدِنِي لِأَحْسَنِ الأَخْلَاقِ"
        ),
        KidsStory(
            id = 3,
            title = "The Kindness to Animals",
            arabicTitle = "الرحمة بالحيوان",
            moral = "A small act of kindness to an animal can earn Allah's greatest reward.",
            storyText = "A thirsty man descended into a deep well to drink. When he climbed out, he saw a panting dog licking mud from thirst. He filled his leather shoe with cool water and gave it to the dog. Allah loved his mercy and forgave him!",
            duaText = "الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ"
        )
    )

    // Hajj & Umrah Guide (Feature 19)
    val hajjSteps = listOf(
        HajjStep(1, true, "Entering Ihram", "الإحرام", "Perform ghusl, wear the two unstitched white towels (for men), and declare Talbiyah from the Miqat.", "لَبَّيْكَ اللَّهُمَّ عُمْرَةً • لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ", "Keep fragrance, cutting hair, and arguments prohibited."),
        HajjStep(2, true, "Tawaf around the Kaaba", "الطواف", "Circumambulate the sacred Kaaba 7 times anti-clockwise starting from the Black Stone (Hajar al-Aswad).", "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ", "Pray 2 rak'ahs behind Maqam Ibrahim if possible."),
        HajjStep(3, true, "Sa'i between Safa & Marwah", "السعي", "Walk 7 laps between the hills of Mount Safa and Mount Marwah honoring Hajar's search for water.", "إِنَّ الصَّفَا وَالْمَرْوَةَ مِن شَعَائِرِ اللَّهِ", "Drink cool Zamzam water abundantly before beginning."),
        HajjStep(4, true, "Halq or Taqsir", "الحلق أو التقصير", "Shave or trim the hair to exit the state of Ihram and complete the Umrah.", "الْحَمْدُ لِلَّهِ الَّذِي قَضَى عَنَّا نُسُكَنَا", "Congratulations! Your Umrah rituals are now fulfilled."),
        HajjStep(5, false, "Day of Tarwiyah at Mina (8th)", "يوم التروية", "Move to the tent city of Mina on the 8th of Dhu al-Hijjah and pray 5 prayers shortened.", "لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ", "Focus on dhikr, reflection, and resting for Arafat."),
        HajjStep(6, false, "Standing at Arafat (9th)", "الوقوف بعرفة", "The central pillar of Hajj. Spend from noon to sunset in passionate du'a on the plains of Arafat.", "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "The best du'a is the du'a of the Day of Arafah."),
        HajjStep(7, false, "Muzdalifah Night & Jamarat", "مزدلفة ورمي الجمرات", "Collect pebbles under the stars at Muzdalifah, then pelt the pillars at Jamarat and offer sacrifice.", "اللَّهُ أَكْبَرُ مَعَ كُلِّ حَصَاةٍ", "Demonstrates rejecting Satan and submitting to Allah.")
    )

    // Good Deeds Sunnah Checklist (Feature 22)
    val defaultGoodDeeds = listOf(
        GoodDeed("deed_1", "Pray Tahajjud (Night Prayer)", "صلاة التهجد", "Worship", "Honor of the believer and peaceful communion with Allah."),
        GoodDeed("deed_2", "Pray Duha (Forenoon Prayer)", "صلاة الضحى", "Worship", "Acts as charity for each of your 360 joints."),
        GoodDeed("deed_3", "Give Daily Sadaqah (Charity)", "الصدقة اليومية", "Charity", "Extinguishes sins just as water puts out fire."),
        GoodDeed("deed_4", "Recite Morning & Evening Adhkar", "أذكار الصباح والمساء", "Remembrance", "Divine armor against anxiety, evil eye, and distress."),
        GoodDeed("deed_5", "Smile at a fellow Muslim", "التبسم في وجه أخيك", "Manners", "Smiling is counted as a rewarded charity in Islam."),
        GoodDeed("deed_6", "Read one Juz or Surah of Quran", "تلاوة القرآن الكريم", "Quran", "Each letter brings 10 rewards in the sight of Allah."),
        GoodDeed("deed_7", "Send Salawat upon the Prophet ﷺ", "الصلاة على النبي ﷺ", "Sunnah", "Whoever sends blessings once, Allah blesses them tenfold.")
    )

    // Istikharah Guide (Feature 23)
    val istikharahDua = """
اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ، فَإِنَّكَ تَقْدِرُ وَلَا أَقْدِرُ، وَتَعْلَمُ وَلَا أَعْلَمُ، وَأَنْتَ عَلَّامُ الْغُيُوبِ. اللَّهُمَّ إِنْ كُنْتَ تَعْلَمُ أَنَّ هَٰذَا الأَمْرَ خَيْرٌ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي، فَاقْدُرْهُ لِي وَيَسِّرْهُ لِي ثُمَّ بَارِكْ لِي فِيهِ، وَإِنْ كُنْتَ تَعْلَمُ أَنَّ هَٰذَا الأَمْرَ شَرٌّ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي، فَاصْرِفْهُ عَنِّي وَاصْرِفْنِي عَنْهُ، وَاقْدُرْ لِيَ الْخَيْرَ حَيْثُ كَانَ ثُمَّ أَرْضِنِي بِهِ.
    """.trimIndent()

    val istikharahTransliteration = """
Allahumma inni astakheeruka bi'ilmika, wa astaqdiruka bi-qudratika, wa as'aluka min fadlikal-'azeem, fa-innaka taqdiru wa la aqdir, wa ta'lamu wa la a'lam, wa Anta 'Allamul-ghuyoob. Allahumma in kunta ta'lamu anna hadhal-amra [name your matter here] khayrun li fee deeni wa ma'ashi wa 'aqibati amri, faqdurhu li wa yassirhu li thumma barik li feeh. Wa in kunta ta'lamu anna hadhal-amra sharrun li fee deeni wa ma'ashi wa 'aqibati amri, fasrifhu 'anni wasrifni 'anhu, waqdur liyal-khayra haythu kana thumma ardini bih.
    """.trimIndent()

    val istikharahTranslation = """
O Allah, I seek Your counsel through Your knowledge, and I seek power through Your ability, and I ask You from Your immense favor. For You have power and I have none, and You know and I do not, and You are the Knower of the unseen. O Allah, if You know that this matter [mention your matter] is good for me in my religion, my livelihood, and the outcome of my affairs, then decree it for me, facilitate it for me, and bless me in it. And if You know that this matter is bad for me in my religion, my livelihood, and the outcome of my affairs, then turn it away from me and turn me away from it, and decree for me what is good wherever it may be, and make me content with it.
    """.trimIndent()

    // Fatwa / Classical Rulings FAQ (Feature 16)
    val fatwas = listOf(
        FatwaItem(1, "Taharah (Purification)", "Does bleeding break wudu according to classical scholars?", "There is a minor difference: Shafi'i madhab holds that bleeding does not break wudu unless it exits from private parts. Hanafi madhab holds flowing blood breaks wudu.", "Fiqh al-Ibadat"),
        FatwaItem(2, "Salah (Prayer)", "What should I do if I forget a rak'ah in prayer?", "Perform Sajdat as-Sahw (prostrations of forgetfulness) — either two prostrations before or after the tasleem, as practiced by the Prophet ﷺ.", "Sahih al-Bukhari & Muslim"),
        FatwaItem(3, "Fasting (Sawm)", "Does using an asthma inhaler invalidate the fast?", "Contemporary scholarly councils (including the International Islamic Fiqh Academy) have ruled that inhalers do not invalidate the fast because it is pressurized gas, not food or drink.", "IIFA Resolution 93"),
        FatwaItem(4, "Zakah", "What is the Nisab threshold for gold and cash savings?", "The Nisab is equivalent to 85 grams of pure gold (approx. 20 mithqals). If personal wealth has been above this for one lunar year, 2.5% is due in Zakah.", "Classical Consensus")
    )

    // Mosques Finder Mock Data (Feature 25)
    val nearbyMosques = listOf(
        MosqueItem("m1", "Al-Haram Sacred Mosque", "Makkah al-Mukarramah", 0.0, 0f, "+966 12 574 0000"),
        MosqueItem("m2", "Al-Masjid an-Nabawi", "Madinah al-Munawwarah", 350.0, 18f, "+966 14 828 0000"),
        MosqueItem("m3", "Central Islamic Center", "Main Downtown Boulevard", 1.8, 45f, "Jummah 1:15 PM"),
        MosqueItem("m4", "Noor Islamic Community Mosque", "72 Garden Avenue", 3.2, 112f, "Jummah 1:30 PM"),
        MosqueItem("m5", "Al-Iman Community Center", "440 West Crescent Road", 5.4, 210f, "Daily 5 prayers")
    )

    // Islamic News (Feature 24)
    val newsArticles = listOf(
        NewsArticle(1, "Millions of Pilgrims Experience Sustainable Cooling at Holy Sites", "Holy Places", "Oct 2026", "New eco-friendly solar shading and misting systems enhance pilgrims' comfort in Makkah and Madinah.", "3 min read"),
        NewsArticle(2, "Restoration of Historic 8th Century Cordoba Quranic Manuscript Completed", "History & Heritage", "Oct 2026", "Master artisans preserve one of the earliest gilded Andalusian Mushafs using advanced laser conservation.", "4 min read"),
        NewsArticle(3, "Global Islamic Relief Reaches Milestone 10 Million Clean Water Wells", "Humanitarian", "Oct 2026", "Community donations provide perpetual Sadaqah Jariyah water systems to drought-impacted regions.", "2 min read")
    )

    // Community Reflection Posts (Feature 27)
    val communityPosts = listOf(
        CommunityPost("c1", "Tariq Mansoor", "London, UK", "Alhamdulillah, finished reciting Surah Al-Kahf today. May Allah grant light between this Friday and the next to all my brothers and sisters!", 42, "2h ago"),
        CommunityPost("c2", "Amina Al-Zahra", "Casablanca, Morocco", "Remember that every single prayer is a scheduled meeting with the King of kings. Don't rush what you want from Him.", 89, "5h ago"),
        CommunityPost("c3", "Dr. Bilal Farooq", "Toronto, Canada", "Started memorizing Juz Amma with my 7-year-old daughter. The barakah in our home has visibly multiplied. SubhanAllah!", 124, "8h ago")
    )
}
