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
    }
}



