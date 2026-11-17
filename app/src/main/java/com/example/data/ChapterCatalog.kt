package com.example.data

import com.example.model.ChapterConfig
import com.example.model.ChapterId
import com.example.model.InventoryItem
import com.example.model.ItemType
import com.example.model.PuzzleType
import com.example.model.Vec3Colors
import com.example.model.WeaponType
import com.example.model.WorldItemSpawn
import com.example.model.WorldNoteSpawn

object ChapterCatalog {

    private fun investigationItems(suffix: String): List<WorldItemSpawn> = listOf(
        WorldItemSpawn(
            markerChar = '8',
            item = InventoryItem(
                id = "cursed_mirror_$suffix",
                nameRu = "Зеркальце (Прокл.)",
                descriptionRu = "Показывает комнату, где сейчас ведьма, но тихо звенит при беге!",
                type = ItemType.MIRROR_CURSED,
                symbol = "🪞"
            ),
            voiceLineRu = "Проклятое зеркальце... В нём видно, где сейчас ходит ведьма! Но при беге оно звенит."
        ),
        WorldItemSpawn(
            markerChar = '9',
            item = InventoryItem(
                id = "black_candle_$suffix",
                nameRu = "Чёрная свеча (Прокл.)",
                descriptionRu = "Ярко освещает темноту и тайные знаки, но ведьма чует её запах.",
                type = ItemType.BLACK_CANDLE,
                symbol = "🕯"
            ),
            voiceLineRu = "Чёрная свеча! С ней видны тайные знаки, но ведьма может учуять её дым."
        ),
        WorldItemSpawn(
            markerChar = 'Z',
            item = InventoryItem(
                id = "tape_$suffix",
                nameRu = "Изолента",
                descriptionRu = "Нужна для починки перерезанного провода дискового телефона 112.",
                type = ItemType.ELECTRICAL_TAPE,
                symbol = "➰"
            ),
            voiceLineRu = "Изолента! Теперь можно починить провод старого телефона и позвонить в 112!"
        )
    )

    val chapters: List<ChapterConfig> = listOf(
        // =========================================================================
        // УРОВЕНЬ 1: ПОДВАЛ-ТЕМНИЦА (Стартуем в камере за решёткой!)
        // =========================================================================
        ChapterConfig(
            id = ChapterId.CHAPTER_1_DUNGEON,
            titleRu = "Уровень 1: Подвал-Темница",
            subtitleRu = "Побег из железного каземата",
            locationRu = "Каземат, винный погреб и котельная",
            roomsListRu = "Камера темницы • Коридор стражи • Винный погреб • Котельная",
            difficultyStars = 1,
            summaryRu = "Совсем другой дом — Чёрная Усадьба у болота. Настя приходит в себя в настоящем подвале-темнице за железной решёткой. Найдите кочергу для самообороны, откройте двери подвала и снимите замок каземата.",
            introStoryLines = listOf(
                "Наш школьный автобус заглох у Чёрного Болота. Когда я с подругами Леной и Машей пошла на свет фонаря, туман сбил нас с ног...",
                "Я очнулась в сыром подвале-темнице. Настоящие железные решётки, тяжёлые дубовые двери и запах угля из котельной.",
                "В углу камеры блестит кованая кочерга — с оружием в руках у меня есть шанс оглушить ведьму и сбежать на первый этаж!"
            ),
            introVoiceRu = "Я очнулась в настоящем подвале-темнице... За решёткой кто-то ходит! Надо взять кочергу в углу камеры, открыть дверь и выбраться наверх.",
            puzzleType = PuzzleType.DUNGEON_SHACKLES,
            puzzleTitleRu = "Замок Каземата",
            puzzleClueRu = "Подсказка узника: «Три цепи держат лестницу: 1. Железо (⛓), 2. Уголь (🔥), 3. Ключ (🗝)».",
            requiredRelicCount = 1,
            witchSpeed = 2.0f,
            witchChaseSpeed = 3.2f,
            witchVisionRange = 11.5f,
            witchHearingRange = 6.5f,
            witchCanTeleport = false,
            witchChecksClosets = false,
            fogColor = Vec3Colors(0.03f, 0.04f, 0.06f),
            ambientLight = Vec3Colors(0.12f, 0.13f, 0.17f),
            torchColor = Vec3Colors(1.0f, 0.56f, 0.22f),
            witchAuraColor = Vec3Colors(0.24f, 0.95f, 0.58f),
            mapGrid = listOf(
                "###############",
                "#S..6I..T..#18#",
                "#.B..I.#.#.#..#",
                "#....D....D...#",
                "####II##.#.####",
                "#2.U.D...T....#",
                "#.BB.#.###.##.#",
                "#H...D..W..D.3#",
                "####.#.###.#O.#",
                "#4.T.#.B...#K.#",
                "#.##D###D###D##",
                "#V...X#...T..Z#",
                "#.H.F.D.P.#.7.#",
                "#..5..#9..#..E#",
                "###############"
            ),
            itemSpawns = listOf(
                WorldItemSpawn(
                    markerChar = '6',
                    item = InventoryItem(
                        id = "iron_poker",
                        nameRu = "Кованая кочерга",
                        descriptionRu = "Оружие ближнего боя: оглушает ведьму на 2.5 секунды.",
                        type = ItemType.WEAPON,
                        symbol = "⚔",
                        weaponType = WeaponType.IRON_POKER
                    ),
                    voiceLineRu = "Отлично! Кованая кочерга — теперь я смогу ударить ведьму и выиграть время!"
                ),
                WorldItemSpawn(
                    markerChar = '1',
                    item = InventoryItem(
                        id = "dungeon_key",
                        nameRu = "Ключ надзирателя",
                        descriptionRu = "Тяжёлый ключ от двери на первый этаж дома.",
                        type = ItemType.KEY,
                        symbol = "🗝"
                    ),
                    voiceLineRu = "Ключ надзирателя! С ним можно открыть тяжёлую дверь у лестницы."
                ),
                WorldItemSpawn(
                    markerChar = '2',
                    item = InventoryItem(
                        id = "prisoner_seal",
                        nameRu = "Железный жетон",
                        descriptionRu = "Нужен для механизма Замка Каземата.",
                        type = ItemType.RELIC,
                        symbol = "⛓"
                    ),
                    voiceLineRu = "Тяжёлый железный жетон с цепью... Подойдёт к замку каземата."
                ),
                WorldItemSpawn(
                    markerChar = '3',
                    item = InventoryItem(
                        id = "salt_dungeon",
                        nameRu = "Соляной оберег",
                        descriptionRu = "Ослепляет ведьму на 4 секунды.",
                        type = ItemType.SALT_WARD,
                        symbol = "✦"
                    ),
                    voiceLineRu = "Мешочек соли у самой печи! Пригодится для защиты."
                ),
                WorldItemSpawn(
                    markerChar = '4',
                    item = InventoryItem(
                        id = "oil_dungeon",
                        nameRu = "Масло для фонаря",
                        descriptionRu = "Полностью восстанавливает заряд фонаря.",
                        type = ItemType.LANTERN_OIL,
                        symbol = "🪔"
                    ),
                    voiceLineRu = "Свежее масло — фонарь снова горит ярко."
                ),
                WorldItemSpawn(
                    markerChar = '5',
                    item = InventoryItem(
                        id = "salt_dungeon_2",
                        nameRu = "Соляной оберег",
                        descriptionRu = "Дополнительный оберег от ведьмы.",
                        type = ItemType.SALT_WARD,
                        symbol = "✦"
                    ),
                    voiceLineRu = "Нашла ещё один соляной оберег!"
                )
            ) + investigationItems("1"),
            noteSpawns = listOf(
                WorldNoteSpawn(
                    markerChar = '7',
                    titleRu = "Журнал стражника",
                    bodyRu = "«Девчонка Маша сбежала на кухню, но её рюкзак остался у котельной. Чтобы поднять решётку наверх, поверни замок: ЖЕЛЕЗО → УГОЛЬ → КЛЮЧ».",
                    voiceLineRu = "Маша выбралась наверх! И порядок для замка: Железо, Уголь, Ключ!"
                )
            ),
            twistTitleRu = "Сюжетный поворот: Рюкзак Маши",
            twistSubtitleRu = "Следы на лестнице",
            twistStoryRu = "Поднявшись из подвала-темницы, Настя находит у дубовой двери расстёгнутый рюкзак своей подруги Маши — и свежие капли воска, ведущие в старую кухню. Дом огромен, и его комнаты будто заперты в проклятом времени: на часах застыла полночь, а из-за дверей доносится звон посуды.",
            twistVoiceRu = "Это рюкзак Маши! Значит, она успела выбежать в кухню. Я должна догнать её и найти Лену!"
        ),

        // =========================================================================
        // УРОВЕНЬ 2: СТАРАЯ КУХНЯ И СТОЛОВАЯ
        // =========================================================================
        ChapterConfig(
            id = ChapterId.CHAPTER_2_KITCHEN,
            titleRu = "Уровень 2: Кухня и Столовая",
            subtitleRu = "Заколдованный пир",
            locationRu = "Кладовая, очаг и банкетный зал",
            roomsListRu = "Прихожая • Кладовая • Старая кухня • Столовая • Буфет",
            difficultyStars = 2,
            summaryRu = "Новые комнаты первого этажа: настоящая кухня с очагом, кладовая и длинный банкетный зал. Отыщите серебряный клинок и разгадайте тайну буфета.",
            introStoryLines = listOf(
                "За дверью подвала открылся первый этаж усадьбы: гулкая столовая, заваленная старинным серебром, и дымная кухня.",
                "Посреди зала накрыт пугающий пир, но еда отравлена чарами — стоит прикоснуться, и дом поглотит твою душу.",
                "Обыщите кладовую и очаг: где-то там спрятан серебряный клинок и ключ дворецкого!"
            ),
            introVoiceRu = "Мы на первом этаже — это старая кухня и банкетный зал. Осторожно, двери скрипят, а ведьма ходит между столами!",
            puzzleType = PuzzleType.KITCHEN_BUFFET,
            puzzleTitleRu = "Сейф Буфета",
            puzzleClueRu = "Порядок подачи блюд из меню: ХЛЕБ (🍞) → ВИНО (🍷) → ЯБЛОКО (🍎) → ЧАША (🏆).",
            requiredRelicCount = 2,
            witchSpeed = 2.3f,
            witchChaseSpeed = 3.55f,
            witchVisionRange = 12.5f,
            witchHearingRange = 8.5f,
            witchCanTeleport = false,
            witchChecksClosets = false,
            fogColor = Vec3Colors(0.05f, 0.03f, 0.03f),
            ambientLight = Vec3Colors(0.14f, 0.10f, 0.09f),
            torchColor = Vec3Colors(1.0f, 0.52f, 0.20f),
            witchAuraColor = Vec3Colors(0.95f, 0.42f, 0.20f),
            mapGrid = listOf(
                "###############",
                "#S..D.T...#1.8#",
                "#.B.#.###.#.B.#",
                "#U..D..2..D..Z#",
                "###.#####.###.#",
                "#3..#..B..#H..#",
                "#.OK#.B.B.#...#",
                "#T..D..W..D..4#",
                "###.#.B.B.#.###",
                "#HX.#V.T..#..5#",
                "#.#D#####D###.#",
                "#6..#...#F...7#",
                "#.#.#.P.#.###.#",
                "#...D.9.D....E#",
                "###############"
            ),
            itemSpawns = listOf(
                WorldItemSpawn(
                    markerChar = '6',
                    item = InventoryItem(
                        id = "silver_stake",
                        nameRu = "Серебряный клинок",
                        descriptionRu = "Мощное оружие: отбрасывает ведьму и оглушает на 3.6 сек!",
                        type = ItemType.WEAPON,
                        symbol = "🗡",
                        weaponType = WeaponType.SILVER_STAKE
                    ),
                    voiceLineRu = "Серебряный охотничий клинок! Вот это настоящее оружие против ведьмы!"
                ),
                WorldItemSpawn(
                    markerChar = '1',
                    item = InventoryItem(
                        id = "silver_ladle",
                        nameRu = "Серебряная чаша",
                        descriptionRu = "Тяжёлая резная чаша из буфета.",
                        type = ItemType.RELIC,
                        symbol = "🏆"
                    ),
                    voiceLineRu = "Серебряная чаша со стола — она открывает замок буфета."
                ),
                WorldItemSpawn(
                    markerChar = '2',
                    item = InventoryItem(
                        id = "menu_scroll",
                        nameRu = "Печать пира",
                        descriptionRu = "Медальон с гербом столовой.",
                        type = ItemType.RELIC,
                        symbol = "🍷"
                    ),
                    voiceLineRu = "Магический медальон банкетного зала!"
                ),
                WorldItemSpawn(
                    markerChar = '3',
                    item = InventoryItem(
                        id = "butler_key",
                        nameRu = "Ключ дворецкого",
                        descriptionRu = "Открывает парадную дверь на второй этаж.",
                        type = ItemType.KEY,
                        symbol = "🗝"
                    ),
                    voiceLineRu = "Ключ дворецкого! Осталось открыть сейф буфета и бежать на второй этаж."
                ),
                WorldItemSpawn(
                    markerChar = '4',
                    item = InventoryItem(
                        id = "salt_kitchen",
                        nameRu = "Соляной оберег",
                        descriptionRu = "Крупная морская соль против чар.",
                        type = ItemType.SALT_WARD,
                        symbol = "✦"
                    ),
                    voiceLineRu = "Отлично, целая горсть освящённой соли!"
                ),
                WorldItemSpawn(
                    markerChar = '5',
                    item = InventoryItem(
                        id = "oil_kitchen",
                        nameRu = "Лампадное масло",
                        descriptionRu = "Восстанавливает заряд фонаря.",
                        type = ItemType.LANTERN_OIL,
                        symbol = "🪔"
                    ),
                    voiceLineRu = "Лампадное масло! Теперь света хватит на весь этаж."
                )
            ) + investigationItems("2"),
            noteSpawns = listOf(
                WorldNoteSpawn(
                    markerChar = '7',
                    titleRu = "Записка Маши на столе",
                    bodyRu = "«Настя! Я видела деревянных кукол за столом — это бывшие гости дома! Не ешь ничего! Код буфета: ХЛЕБ → ВИНО → ЯБЛОКО → ЧАША. Я побежала на второй этаж искать Лену!»",
                    voiceLineRu = "Записка от Маши! Она жива и поднялась на второй этаж. Код буфета: Хлеб, Вино, Яблоко, Чаша!"
                )
            ),
            twistTitleRu = "Сюжетный поворот: За столом — не гости",
            twistSubtitleRu = "Тайна манекенов",
            twistStoryRu = "Когда сейф буфета открывается, за фальшивой панелью обнаруживается портрет хозяйки усадьбы — Матери Воронов. Настя понимает, что ведьма не просто похищает девушек: она готовит ритуал полночи, чтобы перенести свою стареющую душу в одну из пленниц!",
            twistVoiceRu = "Она хочет переселить душу в одну из нас до рассвета! Надо спешить в спальню наверху!"
        ),

        // =========================================================================
        // УРОВЕНЬ 3: СПАЛЬНЯ ВЕДЬМЫ И ЗЕРКАЛЬНАЯ ВАННАЯ
        // =========================================================================
        ChapterConfig(
            id = ChapterId.CHAPTER_3_BEDROOM,
            titleRu = "Уровень 3: Спальня Ведьмы",
            subtitleRu = "Покои Матери Воронов",
            locationRu = "Спальня, гардеробная и зеркальная ванная",
            roomsListRu = "Галерея • Спальня ведьмы • Гардеробная • Зеркальная комната",
            difficultyStars = 3,
            summaryRu = "Второй этаж дома: личные покои ведьмы с кроватью под балдахином, гардеробной и зеркальной комнатой. Здесь спрятан легендарный Арбалет Ведьмобоя!",
            introStoryLines = listOf(
                "Мы поднялись на второй этаж в личные покои ведьмы. Воздух пахнет лавандой и озоном.",
                "В тайнике у гардеробной спрятан охотничий арбалет с серебряными болтами — с ним вы сможете поражать ведьму с расстояния!",
                "Соберите две ноты для шкатулки снов и откройте переход в Болотную Часовню."
            ),
            introVoiceRu = "Это спальня самой ведьмы... Будь осторожна, здесь много зеркал и она умеет появляться из теней! Ищи арбалет!",
            puzzleType = PuzzleType.BEDROOM_MUSICBOX,
            puzzleTitleRu = "Шкатулка Снов",
            puzzleClueRu = "Мелодия сна из дневника: НОЧЬ (🌙) → ПЕРО (🪶) → ЗЕРКАЛО (🪞) → РАССВЕТ (☀).",
            requiredRelicCount = 2,
            witchSpeed = 2.45f,
            witchChaseSpeed = 3.75f,
            witchVisionRange = 13.5f,
            witchHearingRange = 9.5f,
            witchCanTeleport = true,
            witchChecksClosets = false,
            fogColor = Vec3Colors(0.05f, 0.03f, 0.08f),
            ambientLight = Vec3Colors(0.13f, 0.09f, 0.16f),
            torchColor = Vec3Colors(0.85f, 0.45f, 1.0f),
            witchAuraColor = Vec3Colors(0.75f, 0.25f, 1.0f),
            mapGrid = listOf(
                "###############",
                "#S.8D.1.#H.UB.#",
                "#.R.#.###.###.#",
                "#...D..T..DZ.2#",
                "###.#####.###.#",
                "#6..#..R..#.OK#",
                "#.B.#..W..#.R.#",
                "#T..D.....D..3#",
                "###.###.###.###",
                "#HX.#..B..#..4#",
                "#.#D###.###D#.#",
                "#5..#V.T..#F.7#",
                "#.#.#.P.#.###.#",
                "#...D.9.D....E#",
                "###############"
            ),
            itemSpawns = listOf(
                WorldItemSpawn(
                    markerChar = '6',
                    item = InventoryItem(
                        id = "witch_crossbow",
                        nameRu = "Арбалет ведьмобоя",
                        descriptionRu = "Стреляет серебряными болтами, оглушая ведьму издалека на 5 сек!",
                        type = ItemType.WEAPON,
                        symbol = "🏹",
                        weaponType = WeaponType.CROSSBOW
                    ),
                    voiceLineRu = "Настоящий арбалет ведьмобоя! Теперь я могу отбиваться даже с другого конца коридора!"
                ),
                WorldItemSpawn(
                    markerChar = '4',
                    item = InventoryItem(
                        id = "silver_bolts_3",
                        nameRu = "Серебряные болты (×3)",
                        descriptionRu = "Боеприпасы для арбалета.",
                        type = ItemType.AMMO_BOLT,
                        symbol = "➶"
                    ),
                    voiceLineRu = "Серебряные болты для арбалета! Береги каждый выстрел."
                ),
                WorldItemSpawn(
                    markerChar = '1',
                    item = InventoryItem(
                        id = "dream_cylinder",
                        nameRu = "Валик шкатулки",
                        descriptionRu = "Резной золотой валик с мелодией.",
                        type = ItemType.RELIC,
                        symbol = "🎵"
                    ),
                    voiceLineRu = "Золотой валик от шкатулки снов!"
                ),
                WorldItemSpawn(
                    markerChar = '2',
                    item = InventoryItem(
                        id = "raven_brooch",
                        nameRu = "Брошь с пером",
                        descriptionRu = "Украшение Матери Воронов.",
                        type = ItemType.RELIC,
                        symbol = "🪶"
                    ),
                    voiceLineRu = "Брошь ведьмы! Она тоже нужна для шкатулки."
                ),
                WorldItemSpawn(
                    markerChar = '3',
                    item = InventoryItem(
                        id = "chapel_key",
                        nameRu = "Ключ от часовни",
                        descriptionRu = "Ведёт в внутреннюю болотную часовню дома.",
                        type = ItemType.KEY,
                        symbol = "🗝"
                    ),
                    voiceLineRu = "Ключ от болотной часовни! Осталось завести шкатулку."
                ),
                WorldItemSpawn(
                    markerChar = '5',
                    item = InventoryItem(
                        id = "salt_bedroom",
                        nameRu = "Соляной оберег",
                        descriptionRu = "Ослепляет ведьму при приближении.",
                        type = ItemType.SALT_WARD,
                        symbol = "✦"
                    ),
                    voiceLineRu = "Ещё один оберег! На всякий случай."
                )
            ) + investigationItems("3"),
            noteSpawns = listOf(
                WorldNoteSpawn(
                    markerChar = '7',
                    titleRu = "Личный дневник Ведьмы",
                    bodyRu = "«Чтобы удержать молодость, я заперла Лену и Машу под куполом башни. Но младшая девчонка Настя выбралась из темницы и нашла оружие! Мелодия шкатулки: НОЧЬ → ПЕРО → ЗЕРКАЛО → РАССВЕТ».",
                    voiceLineRu = "Лена и Маша в Башне Ворона! А мелодия для шкатулки: Ночь, Перо, Зеркало, Рассвет!"
                )
            ),
            twistTitleRu = "Сюжетный поворот: Встреча с Машей",
            twistSubtitleRu = "Голос за витражом",
            twistStoryRu = "Шкатулка играет хрустальную колыбельную, и потайная дверь в старую часовню открывается! На пороге Настя видит бледную, но живую Машу — та передаёт связку серебряных болтов и шепчет: «Беги через болотную часовню в башню, я ударю в колокол и отвлеку её!»",
            twistVoiceRu = "Маша! Как я рада, что ты цела! Держись, я спасу Лену и мы выберемся отсюда вместе!"
        ),

        // =========================================================================
        // УРОВЕНЬ 4: БОЛОТНАЯ ЧАСОВНЯ И СКЛЕП
        // =========================================================================
        ChapterConfig(
            id = ChapterId.CHAPTER_4_CHAPEL,
            titleRu = "Уровень 4: Болотная Часовня",
            subtitleRu = "Зал органа и ядовитой топи",
            locationRu = "Домашняя часовня, склеп и мостик над болотом",
            roomsListRu = "Притвор • Зал со скамьями • Болотный пролом • Органная • Склеп",
            difficultyStars = 4,
            summaryRu = "Новые мрачные помещения на краю дома, где крыло усадьбы ушло в ядовитое болото. Избегайте болотных испарений, пополняйте болты и сыграйте на органе часовни.",
            introStoryLines = listOf(
                "Восточное крыло усадьбы рухнуло прямо в Чёрное Болото: сквозь пол часовни пробивается ядовитый зелёный туман.",
                "Маша ударила в колокол, но ведьма в ярости — она телепортируется быстрее и патрулирует между церковными скамьями.",
                "Соберите 3 нотных свитка и сыграйте аккорд освобождения на старинном органе!"
            ),
            introVoiceRu = "Болото просочилось прямо в часовню дома! Не задерживайся в фиолетовом тумане и экономь серебряные болты!",
            puzzleType = PuzzleType.CHAPEL_ORGAN,
            puzzleTitleRu = "Орган Часовни",
            puzzleClueRu = "Партитура на пюпитре: «1. ДО (Тьма) → 2. МИ (Ветер) → 3. СОЛЬ (Серебро) → 4. СИ (Рассвет)».",
            requiredRelicCount = 3,
            witchSpeed = 2.65f,
            witchChaseSpeed = 3.95f,
            witchVisionRange = 14.5f,
            witchHearingRange = 10.5f,
            witchCanTeleport = true,
            witchChecksClosets = false,
            fogColor = Vec3Colors(0.02f, 0.06f, 0.04f),
            ambientLight = Vec3Colors(0.10f, 0.16f, 0.13f),
            torchColor = Vec3Colors(0.35f, 0.96f, 0.62f),
            witchAuraColor = Vec3Colors(0.25f, 1.0f, 0.45f),
            mapGrid = listOf(
                "###############",
                "#S8.D.1.#UH..2#",
                "#.B.#.#.#.###.#",
                "#Z.M..D..T..M.#",
                "###.#####.###.#",
                "#3..#..B..#..4#",
                "#.M.#.B.B.#.M.#",
                "#T..D..W..D.OK#",
                "###.#.B.B.#.###",
                "#6X.#V.M..#..5#",
                "#.#D#####D###.#",
                "#H..#..T..#F.7#",
                "#.#.#.P.#.###.#",
                "#...D.9.D....E#",
                "###############"
            ),
            itemSpawns = listOf(
                WorldItemSpawn(
                    markerChar = '1',
                    item = InventoryItem(
                        id = "organ_scroll_1",
                        nameRu = "Свиток «Тьма»",
                        descriptionRu = "Первая нотная запись для органа.",
                        type = ItemType.RELIC,
                        symbol = "🎼"
                    ),
                    voiceLineRu = "Первый нотный свиток для органа!"
                ),
                WorldItemSpawn(
                    markerChar = '2',
                    item = InventoryItem(
                        id = "organ_scroll_2",
                        nameRu = "Свиток «Серебро»",
                        descriptionRu = "Вторая партитура органа часовни.",
                        type = ItemType.RELIC,
                        symbol = "🎼"
                    ),
                    voiceLineRu = "Второй свиток партитуры найден!"
                ),
                WorldItemSpawn(
                    markerChar = '3',
                    item = InventoryItem(
                        id = "organ_scroll_3",
                        nameRu = "Свиток «Рассвет»",
                        descriptionRu = "Третья священная нота.",
                        type = ItemType.RELIC,
                        symbol = "🎼"
                    ),
                    voiceLineRu = "Третий свиток! Можно идти к органу."
                ),
                WorldItemSpawn(
                    markerChar = '4',
                    item = InventoryItem(
                        id = "tower_key",
                        nameRu = "Ключ от Башни Ворона",
                        descriptionRu = "Открывает винтовую лестницу на крышу.",
                        type = ItemType.KEY,
                        symbol = "🗝"
                    ),
                    voiceLineRu = "Ключ от Башни Ворона у меня! Совсем чуть-чуть до финала."
                ),
                WorldItemSpawn(
                    markerChar = '5',
                    item = InventoryItem(
                        id = "salt_chapel",
                        nameRu = "Соляной оберег",
                        descriptionRu = "Освящённая соль часовни.",
                        type = ItemType.SALT_WARD,
                        symbol = "✦"
                    ),
                    voiceLineRu = "Освящённая соль! Сразу спокойнее на душе."
                ),
                WorldItemSpawn(
                    markerChar = '6',
                    item = InventoryItem(
                        id = "chapel_bolts",
                        nameRu = "Серебряные болты (×4)",
                        descriptionRu = "Запас болтов для арбалета.",
                        type = ItemType.AMMO_BOLT,
                        symbol = "➶"
                    ),
                    voiceLineRu = "Целая связка серебряных болтов! Теперь я готова к встрече с ведьмой."
                )
            ) + investigationItems("4"),
            noteSpawns = listOf(
                WorldNoteSpawn(
                    markerChar = '7',
                    titleRu = "Ноты старого священника",
                    bodyRu = "«Аккорд часовни отпирает путь в башню: ДО (Тьма) → МИ (Ветер) → СОЛЬ (Серебро) → СИ (Рассвет). На вершине башни разорви Корону Ворона — иначе болото поглотит всех троих».",
                    voiceLineRu = "Аккорд органа: До, Ми, Соль, Си! Запомнила, бежим к органу!"
                )
            ),
            twistTitleRu = "Сюжетный поворот: Настоящее имя ведьмы",
            twistSubtitleRu = "Правда под алтарём",
            twistStoryRu = "Мощный аккорд органа сотрясает своды часовни. Из ниши за алтарём выпадает старинное зеркальце: хозяйка Чёрной Усадьбы — сестра-близнец пропавшей смотрительницы маяка, проклятая 80 лет назад! Наверху, в Башне Ворона, она уже начала финальный ритуал над Леной...",
            twistVoiceRu = "Орган открыл лестницу в башню! Лена в опасности, поднимаемся на самый верх!"
        ),

        // =========================================================================
        // УРОВЕНЬ 5: БАШНЯ ВОРОНА И КРЫША (ФИНАЛ)
        // =========================================================================
        ChapterConfig(
            id = ChapterId.CHAPTER_5_TOWER,
            titleRu = "Уровень 5: Башня Ворона",
            subtitleRu = "Финальный бой и рассвет",
            locationRu = "Комната ворон, ритуальный зал и крыша",
            roomsListRu = "Ярус ворон • Арсенал • Алтарь Короны • Выход на крышу",
            difficultyStars = 4,
            summaryRu = "Финал! Ведьма впадает в неистовство и проверяет шкафы, если видит, как вы прячетесь. Используйте арбалет и клинок, активируйте 5 узлов Короны Ворона и выведите подруг на свободу!",
            introStoryLines = listOf(
                "Вершина Чёрной Усадьбы — круглая Башня Ворона. Ветхие окна открывают вид на далёкий рассвет за болотом.",
                "Осторожно: здесь ведьма видит всё и проверяет шкафы, если заметила, как вы прятались! Отбивайтесь оружием!",
                "Соберите 3 печати, завершите ритуал Короны Ворона в центре зала и откройте дверь на крышу к канатной дороге!"
            ),
            introVoiceRu = "Мы на самой вершине — Башня Ворона! Лена где-то у выхода. Держи оружие наготове, я должна разрушить Корону Ворона!",
            puzzleType = PuzzleType.TOWER_CROWN,
            puzzleTitleRu = "Ритуал Короны Ворона",
            puzzleClueRu = "Повторите вспышки пяти перьев Короны Ворона (3 круга), чтобы уничтожить проклятие усадьбы.",
            requiredRelicCount = 3,
            witchSpeed = 2.85f,
            witchChaseSpeed = 4.15f,
            witchVisionRange = 15.5f,
            witchHearingRange = 11.5f,
            witchCanTeleport = true,
            witchChecksClosets = true,
            fogColor = Vec3Colors(0.06f, 0.02f, 0.09f),
            ambientLight = Vec3Colors(0.12f, 0.08f, 0.16f),
            torchColor = Vec3Colors(0.82f, 0.48f, 1.0f),
            witchAuraColor = Vec3Colors(1.0f, 0.18f, 0.34f),
            mapGrid = listOf(
                "###############",
                "#S8.D.1.#U6..2#",
                "#.B.#.#.#.###.#",
                "#...D..T..DZ..#",
                "###.#####.###.#",
                "#3..#.OK..#..H#",
                "#.R.#.P.B.#.R.#",
                "#T..D..W..D..4#",
                "###.#.....#.###",
                "#5X.#V.T..#..7#",
                "#.#D#####D###.#",
                "#H..#.....#F..#",
                "#.#.#.R.R.#.#.#",
                "#...D..9..D..E#",
                "###############"
            ),
            itemSpawns = listOf(
                WorldItemSpawn(
                    markerChar = '1',
                    item = InventoryItem(
                        id = "raven_heart",
                        nameRu = "Сердце Ворона",
                        descriptionRu = "Чёрный кристалл башни.",
                        type = ItemType.RELIC,
                        symbol = "🖤"
                    ),
                    voiceLineRu = "Сердце Ворона — первая печать для ритуала!"
                ),
                WorldItemSpawn(
                    markerChar = '2',
                    item = InventoryItem(
                        id = "moon_feather",
                        nameRu = "Лунное перо",
                        descriptionRu = "Сияющее серебряное перо.",
                        type = ItemType.RELIC,
                        symbol = "🪶"
                    ),
                    voiceLineRu = "Лунное перо найдено! Осталась последняя реликвия."
                ),
                WorldItemSpawn(
                    markerChar = '3',
                    item = InventoryItem(
                        id = "dawn_seal",
                        nameRu = "Печать Рассвета",
                        descriptionRu = "Ключевой камень освобождения.",
                        type = ItemType.RELIC,
                        symbol = "💎"
                    ),
                    voiceLineRu = "Печать Рассвета! Все три артефакта у меня — бежим к центру башни!"
                ),
                WorldItemSpawn(
                    markerChar = '4',
                    item = InventoryItem(
                        id = "roof_key",
                        nameRu = "Ключ от крыши",
                        descriptionRu = "Открывает люк к канатной дороге через болото.",
                        type = ItemType.KEY,
                        symbol = "🗝"
                    ),
                    voiceLineRu = "Ключ от выхода на крышу! Пора разрушить Корону Ворона!"
                ),
                WorldItemSpawn(
                    markerChar = '5',
                    item = InventoryItem(
                        id = "salt_tower",
                        nameRu = "Соль Очищения",
                        descriptionRu = "Мощный соляной оберег.",
                        type = ItemType.SALT_WARD,
                        symbol = "✦"
                    ),
                    voiceLineRu = "Соль очищения — отличный запас в финальной комнате!"
                ),
                WorldItemSpawn(
                    markerChar = '6',
                    item = InventoryItem(
                        id = "tower_bolts",
                        nameRu = "Серебряные болты (×5)",
                        descriptionRu = "Полный колчан для финального боя с ведьмой.",
                        type = ItemType.AMMO_BOLT,
                        symbol = "➶"
                    ),
                    voiceLineRu = "Пять серебряных болтов! Попробуй теперь подойди ко мне, ведьма!"
                )
            ) + investigationItems("5"),
            noteSpawns = listOf(
                WorldNoteSpawn(
                    markerChar = '7',
                    titleRu = "Последняя записка Лены",
                    bodyRu = "«Настя! Маша уже внизу у канатной дороги, а я держу трос на крыше! Как только разрушишь Корону Ворона в центре башни, беги к двери с ключом — мы спасёмся все втроём!»",
                    voiceLineRu = "Лена и Маша ждут на крыше у канатной дороги! Разрушим Корону и бежим!"
                )
            ),
            twistTitleRu = "Финал: Рассвет над Чёрным Болотом",
            twistSubtitleRu = "Три подруги на свободе",
            twistStoryRu = "Пятая руна Короны Ворона вспыхивает нестерпимым серебром! Проклятый трон раскалывается, а сама Чёрная Усадьба начинает медленно оседать в болотный туман. Настя выбивает засов крыши, обнимает плачущих от радости Лену и Машу, и все трое скользят по канатной дороге над туманом навстречу золотому утреннему солнцу!",
            twistVoiceRu = "Лена! Маша! Мы сделали это! Чёрная усадьба рушится позади... Мы наконец-то свободны!"
        )
    )

    fun getChapter(id: ChapterId): ChapterConfig = chapters.first { it.id == id }
}
