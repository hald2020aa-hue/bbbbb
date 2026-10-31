package com.example

import com.example.data.LiveMangaSiteScraper
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun parseChapters_nestedEplisterWithHeaderDivAndBtnLinks_extractsAllChapters() {
        val html = """
            <div class="eplister" id="chapterlist">
                <div class="ephead">
                    <span>رقم الفصل</span>
                    <span>تاريخ النشر</span>
                </div>
                <div class="ts-chl-collapsible-content">
                    <ul>
                        <li>
                            <a href="https://olympustaff.com/series/murim-psychopath/3" class="btn chapter-btn">
                                <div class="chbox">
                                    <div class="eph-num">
                                        <span class="chapternum">الفصل 3</span>
                                        <span class="chapterdate">2026-10-30</span>
                                    </div>
                                </div>
                            </a>
                        </li>
                        <li>
                            <div class="chbox">
                                <div class="ch-icon"><span>NEW</span></div>
                                <div class="eph-num">
                                    <a href="https://olympustaff.com/series/murim-psychopath/2">
                                        <span class="chapternum">الفصل 2</span>
                                        <span class="chapterdate">2026-10-25</span>
                                    </a>
                                </div>
                            </div>
                        </li>
                        <li>
                            <a href="/series/murim-psychopath/1">الفصل 1</a>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="related-manga">
                <a href="https://olympustaff.com/series/other-work/99">الفصل 99</a>
            </div>
        """.trimIndent()

        val chapters = LiveMangaSiteScraper.parseChaptersFromHtmlFragment(
            html = html,
            workId = "murim-psychopath",
            pageUrl = "https://olympustaff.com/series/murim-psychopath"
        )

        assertEquals(3, chapters.size)
        assertEquals(listOf(1, 2, 3), chapters.map { it.number })
        assertEquals("https://olympustaff.com/series/murim-psychopath/3", chapters[2].chapterUrl)
    }

    @Test
    fun parseChapters_percentEncodedArabicChapterUrls_extractsCorrectly() {
        val html = """
            <div class="page-content-listing">
                <div class="header-inner"><span>الفصول</span></div>
                <ul class="main version-chap">
                    <li class="wp-manga-chapter">
                        <a href="https://lekmanga.net/manga/murim-psychopath/%D8%A7%D9%84%D9%81%D8%B5%D9%84-12/">
                            الفصل 12
                        </a>
                        <span class="chapter-release-date"><i>منذ يومين</i></span>
                    </li>
                    <li class="wp-manga-chapter">
                        <a href="https://lekmanga.net/manga/murim-psychopath/%D9%81%D8%B5%D9%84-11/">
                            الفصل 11
                        </a>
                    </li>
                </ul>
            </div>
        """.trimIndent()

        val chapters = LiveMangaSiteScraper.parseChaptersFromHtmlFragment(
            html = html,
            workId = "murim-psychopath",
            pageUrl = "https://lekmanga.net/manga/murim-psychopath/"
        )

        assertEquals(2, chapters.size)
        assertEquals(listOf(11, 12), chapters.map { it.number })
    }

    @Test
    fun parseChapters_singleChapterGalleryWith12PageThumbnails_returnsSingleChapterWith12Pages() {
        val thumbsHtml = (1..12).joinToString("\n") { pageNum ->
            """
            <div class="thumb-container">
                <a class="gallerythumb" href="/g/485123/$pageNum/">
                    <img src="https://t3.nhentai.net/galleries/2999111/${pageNum}t.jpg" data-src="https://t3.nhentai.net/galleries/2999111/${pageNum}t.jpg" />
                </a>
            </div>
            """.trimIndent()
        }
        val html = """
            <div id="info">
                <h1>ブルア... [えだまめ亭 (よろず)]</h1>
                <div class="tags">
                    <a href="/tag/doujinshi/" class="tag">Doujinshi</a>
                </div>
            </div>
            <div class="container" id="thumbnail-container">
                $thumbsHtml
            </div>
        """.trimIndent()

        val pageUrl = "https://nhentai.net/g/485123/"
        val multiChapters = LiveMangaSiteScraper.parseChaptersFromHtmlFragment(
            html = html,
            workId = "g_485123",
            pageUrl = pageUrl
        )
        // Page thumbnails /1/ .. /12/ inside #thumbnail-container must NOT be mistaken for 12 separate chapters
        assertTrue("Page thumbnails should not be parsed as separate chapters", multiChapters.isEmpty())

        // And single-chapter gallery extractor should collect all 12 full-resolution pages into 1 chapter
        val galleryPages = LiveMangaSiteScraper.extractSingleChapterGalleryPages(html, pageUrl)
        assertEquals(12, galleryPages.size)
        assertEquals("https://i3.nhentai.net/galleries/2999111/1.jpg", galleryPages.first())
        assertEquals("https://i3.nhentai.net/galleries/2999111/12.jpg", galleryPages.last())
    }

    @Test
    fun parseCatalogHtml_handlesCustomHentaiPathsAndWwwNormalization() {
        val site = com.example.data.local.SiteEntity(
            id = 77L,
            name = "HentaiRead",
            url = "https://hentairead.com",
            domain = "hentairead.com",
            iconKey = "globe"
        )
        val html = """
            <html>
            <body>
                <div class="grid">
                    <a href="https://www.hentairead.com/hentai/blue-archive-special/" title="Blue Archive Special">
                        <img src="https://www.hentairead.com/wp-content/uploads/cover1.webp" alt="Blue Archive Special" />
                    </a>
                    <a href="https://hentairead.com/manga/crimson-knight/" title="Crimson Knight">
                        <img src="https://hentairead.com/wp-content/uploads/cover2.webp" alt="Crimson Knight" />
                    </a>
                </div>
            </body>
            </html>
        """.trimIndent()

        val works = LiveMangaSiteScraper.parseCatalogHtml(html, site, "https://www.hentairead.com/")
        assertEquals(2, works.size)
        assertEquals("Blue Archive Special", works[0].title)
        assertEquals("Crimson Knight", works[1].title)

        // Verify Arabic Cloudflare interstitial detection
        val cfArabicHtml = "<html><head><title>انتظر لحظة...</title></head><body>يتم الآن التحقق من أنك إنسان. قد يستغرق هذا بضع ثوانٍ.</body></html>"
        assertTrue(LiveMangaSiteScraper.isCloudflareChallengeHtml(cfArabicHtml, 403))

        // Verify exact Arabic Cloudflare Turnstile page from video ("لحظة واحدة...", "إجراء التحقق من الأمان", "يستخدم هذا الموقع خدمة أمنية للحماية")
        val cfVideoHtml = """
            <html>
            <head><title>لحظة واحدة...</title></head>
            <body>
                <h1 class="zone-name-title">hentairead.com</h1>
                <h2 id="challenge-body-text">إجراء التحقق من الأمان</h2>
                <div>يستخدم هذا الموقع خدمة أمنية للحماية من برامج الروبوتات الضارة. يتحقق موقع الويب من أنك لست روبوتًا.</div>
                <div>Ray ID: a5320bb219e64cc9</div>
                <div>الأداء والأمان من Cloudflare</div>
            </body>
            </html>
        """.trimIndent()
        assertTrue(LiveMangaSiteScraper.isCloudflareChallengeHtml(cfVideoHtml, 200))
        assertTrue(LiveMangaSiteScraper.isCloudflareChallengeTitle("لحظة واحدة..."))
        assertTrue(LiveMangaSiteScraper.isCloudflareChallengeTitle("إجراء التحقق من الأمان"))
        assertFalse(LiveMangaSiteScraper.isCloudflareChallengeTitle("HentaiRead - Read Manga Online"))

        // Verify User-Agent sanitization removes "; wv" and "Version/4.0" while preserving exact Chrome version & device model
        val rawWebViewUa = "Mozilla/5.0 (Linux; Android 14; SM-S918B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.6723.106 Mobile Safari/537.36"
        val sanitizedUa = LiveMangaSiteScraper.sanitizeWebViewUserAgent(rawWebViewUa)
        assertFalse(sanitizedUa.contains("; wv"))
        assertFalse(sanitizedUa.contains("Version/4.0"))
        assertTrue(sanitizedUa.contains("Chrome/130.0.6723.106"))
        assertTrue(sanitizedUa.contains("SM-S918B"))

        // Verify unwrapEvaluateJsString unwraps double-encoded JSON returned by WebView.evaluateJavascript
        val doubleEncoded = "\"{\\\"cf\\\":false,\\\"items\\\":[{\\\"title\\\":\\\"Test\\\",\\\"url\\\":\\\"https://hentairead.com/hentai/test/\\\",\\\"cover\\\":\\\"https://hentairead.com/c.jpg\\\"}]}\""
        val unwrapped = LiveMangaSiteScraper.unwrapEvaluateJsString(doubleEncoded)
        assertTrue(unwrapped.contains("\"cf\":false"))
        assertTrue(unwrapped.contains("\"items\":[{"))

        // Verify pure-Kotlin challenge title check avoids JS evaluation during Turnstile
        assertTrue(LiveMangaSiteScraper.isLikelyStillOnChallengeOrLoadingTitle("لحظة واحدة...", "https://hentairead.com/"))
        assertTrue(LiveMangaSiteScraper.isLikelyStillOnChallengeOrLoadingTitle("hentairead.com", "https://hentairead.com/"))
        assertTrue(LiveMangaSiteScraper.isLikelyStillOnChallengeOrLoadingTitle("", "https://hentairead.com/"))
        assertFalse(LiveMangaSiteScraper.isLikelyStillOnChallengeOrLoadingTitle("HentaiRead - Read Manga Online", "https://hentairead.com/"))
    }

    @Test
    fun parseCatalogHtml_extractsAllMangaAndManhwaCardsIncludingSeparateThumbnailAndTitleLinks() {
        val site = com.example.data.local.SiteEntity(
            id = 88L,
            name = "Olympus",
            url = "https://olympustaff.com",
            domain = "olympustaff.com",
            iconKey = "globe"
        )
        // Includes:
        // 1) Separate thumbnail <a><img></a> (no alt/title) + sibling <h3><a>Title</a></h3>
        // 2) Slugs containing words like "random", "popular", "history" that previously got rejected
        // 3) Cover <img> outside the <a> tag inside a wrapper div
        val html = """
            <html>
            <body>
                <div class="featured-slider">
                    <div class="slide-item">
                        <a href="https://olympustaff.com/series/solo-leveling-ragnarok/">
                            <img src="https://olympustaff.com/uploads/solo.webp" />
                        </a>
                        <div class="slide-info">
                            <h3><a href="https://olympustaff.com/series/solo-leveling-ragnarok/">Solo Leveling: Ragnarok</a></h3>
                        </div>
                    </div>
                </div>
                <div class="listupd">
                    <div class="bs">
                        <div class="bsx">
                            <a href="https://olympustaff.com/manga/random-chat/">
                                <div class="limit"><img src="https://olympustaff.com/uploads/random-chat.jpg" /></div>
                            </a>
                            <div class="bigor">
                                <div class="tt"><a href="https://olympustaff.com/manga/random-chat/">Random Chat Manhwa</a></div>
                            </div>
                        </div>
                    </div>
                    <div class="bs">
                        <div class="bsx">
                            <a href="https://olympustaff.com/manga/popular-martial-god/">
                                <div class="limit"><img data-src="https://olympustaff.com/uploads/martial-god.jpg" /></div>
                            </a>
                            <div class="bigor">
                                <h3><a href="https://olympustaff.com/manga/popular-martial-god/">Popular Martial God</a></h3>
                            </div>
                        </div>
                    </div>
                    <div class="custom-card">
                        <div class="thumb-wrap">
                            <img src="https://olympustaff.com/uploads/history-kingdoms.png" />
                        </div>
                        <div class="info-wrap">
                            <h4><a href="https://olympustaff.com/manhwa/history-of-three-kingdoms/">History of Three Kingdoms</a></h4>
                        </div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        val works = LiveMangaSiteScraper.parseCatalogHtml(html, site, "https://olympustaff.com/")
        assertEquals(4, works.size)
        assertEquals(
            listOf(
                "Solo Leveling: Ragnarok",
                "Random Chat Manhwa",
                "Popular Martial God",
                "History of Three Kingdoms"
            ),
            works.map { it.title }
        )
    }
}
