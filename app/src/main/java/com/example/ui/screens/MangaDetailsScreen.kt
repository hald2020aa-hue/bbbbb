package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LiveMangaSiteScraper
import com.example.data.MangaChapter
import com.example.data.MangaWork
import com.example.data.local.DownloadedChapterEntity
import com.example.data.local.SiteEntity
import com.example.ui.components.CloudflareProtectionDialog
import com.example.ui.components.HiddenSiteWebViewBridge
import com.example.ui.components.MangaCoverArtwork
import com.example.ui.components.ShimmerChaptersList
import com.example.ui.components.SiteIconBadge
import com.example.ui.components.rememberDynamicCoverPalette
import com.example.ui.theme.AppBackgroundDark
import com.example.ui.theme.AppDialogSurface
import com.example.ui.theme.AppDividerColor
import com.example.ui.theme.PeriwinkleOnPrimary
import com.example.ui.theme.PeriwinklePrimary
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGrey
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaDetailsScreen(
    work: MangaWork,
    isFavorite: Boolean,
    downloadedChapterNumbers: Set<Int>,
    chapterDownloadStates: Map<Int, DownloadedChapterEntity> = emptyMap(),
    readChapterNumbers: Set<Int> = emptySet(),
    resumeLabel: String = "",
    addedSites: List<SiteEntity> = emptyList(),
    categories: List<String> = listOf("أقرأها حالياً", "مكتملة", "مؤجلة", "أرغب بقراءتها"),
    currentCategory: String = "أقرأها حالياً",
    onAssignCategory: (MangaWork, String) -> Unit = { _, _ -> },
    onExportChapterCbz: (MangaWork, MangaChapter) -> Unit = { _, _ -> },
    onExportChapterPdf: (MangaWork, MangaChapter) -> Unit = { _, _ -> },
    onExportAllCbz: (MangaWork) -> Unit = {},
    isSearchingMigration: Boolean = false,
    migrationCandidates: List<MangaWork> = emptyList(),
    onFindMigrationCandidates: (MangaWork) -> Unit = {},
    onMigrateToWork: (MangaWork, MangaWork) -> Unit = { _, _ -> },
    onBackClick: () -> Unit,
    onOpenChapter: (MangaWork, MangaChapter) -> Unit,
    onToggleFavorite: (MangaWork) -> Unit,
    onSaveWorkToStorage: (MangaWork) -> Unit,
    onDownloadChapters: (MangaWork, List<MangaChapter>, String) -> Unit,
    onDeleteSingleDownloadedChapter: (MangaWork, MangaChapter) -> Unit = { _, _ -> },
    onRemoveWorkFromStorage: (String) -> Unit = {},
    onOpenInWebView: (String, String) -> Unit,
    onSelectSiteForSearch: ((SiteEntity) -> Unit)? = null,
    onMarkChaptersReadBatch: (MangaWork, List<Int>, Boolean) -> Unit = { _, _, _ -> },
    onOpenAlternativeSourceChapter: (MangaWork, Int) -> Unit = { _, _ -> },
    onWorkDetailsUpdated: (MangaWork) -> Unit = {},
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val targetMangaUrl = remember(work.mangaUrl, work.siteUrl, work.id) {
        work.mangaUrl.ifBlank {
            if (work.siteUrl.isNotBlank()) "${work.siteUrl.trimEnd('/')}/series/${work.id}" else ""
        }
    }
    val hasValidRemoteUrl = remember(targetMangaUrl) {
        targetMangaUrl.startsWith("http://", ignoreCase = true) || targetMangaUrl.startsWith("https://", ignoreCase = true)
    }

    val detailsContext = androidx.compose.ui.platform.LocalContext.current

    var liveWork by remember(work.id, work.mangaUrl, work.chapters.size, work.storageSizeBytes) {
        val cached = LiveMangaSiteScraper.getCachedWorkDetails(work.id, targetMangaUrl)
        val initialWork = if (work.chapters.isEmpty() && cached != null && cached.chapters.isNotEmpty()) {
            cached.copy(
                localCoverPath = work.localCoverPath.ifBlank { cached.localCoverPath },
                isFromLocalStorage = work.isFromLocalStorage,
                storageSizeBytes = maxOf(work.storageSizeBytes, cached.storageSizeBytes)
            )
        } else {
            work
        }
        mutableStateOf(initialWork)
    }
    var isLoadingChapters by remember(work.id, targetMangaUrl) {
        mutableStateOf(liveWork.chapters.isEmpty() && hasValidRemoteUrl)
    }
    var requiresCloudflare by remember(work.id, targetMangaUrl) { mutableStateOf(false) }
    var showProtectionDialog by remember(work.id, targetMangaUrl) { mutableStateOf(false) }
    var hasAutoOpenedProtection by remember(work.id, targetMangaUrl) { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(requiresCloudflare, liveWork.chapters.size) {
        if (requiresCloudflare && liveWork.chapters.isEmpty() && !hasAutoOpenedProtection) {
            hasAutoOpenedProtection = true
            showProtectionDialog = true
        }
    }

    var isChaptersReversed by remember { mutableStateOf(false) }
    var chapterFilterMode by remember { mutableStateOf("الكل") } // الكل | غير المقروءة | المحملة
    var chapterJumpQuery by remember { mutableStateOf("") }
    var showChapterSearchBar by remember { mutableStateOf(false) }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedChapterNums by remember(work.id) { mutableStateOf(setOf<Int>()) }
    val chaptersListState = rememberLazyListState()
    var activeChapterMenuId by remember { mutableStateOf<String?>(null) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showSearchInSitesDialog by remember { mutableStateOf(false) }
    var showDownloadMangaDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showMigrationDialog by remember { mutableStateOf(false) }
    var bottomActionHint by remember { mutableStateOf<String?>(null) }

    fun mergeRemoteAndLocalChapters(
        remoteChapters: List<MangaChapter>,
        existingChapters: List<MangaChapter>
    ): List<MangaChapter> {
        if (remoteChapters.isEmpty()) return existingChapters
        if (existingChapters.isEmpty()) return remoteChapters
        val existingByNum = existingChapters.associateBy { it.number }
        val mergedMap = LinkedHashMap<Int, MangaChapter>()
        for (remoteCh in remoteChapters) {
            val localCh = existingByNum[remoteCh.number]
            if (localCh != null && (localCh.localPagePaths.isNotEmpty() || localCh.isDownloaded || localCh.lastReadPage >= 0)) {
                mergedMap[remoteCh.number] = remoteCh.copy(
                    localPagePaths = localCh.localPagePaths,
                    downloadedPagesCount = localCh.downloadedPagesCount.coerceAtLeast(localCh.localPagePaths.size),
                    pageCount = localCh.pageCount.coerceAtLeast(remoteCh.pageCount),
                    isDownloaded = localCh.isDownloaded,
                    downloadStatus = localCh.downloadStatus,
                    downloadError = localCh.downloadError,
                    isRead = localCh.isRead || remoteCh.isRead,
                    lastReadPage = localCh.lastReadPage,
                    lastReadTimeText = localCh.lastReadTimeText.ifBlank { remoteCh.lastReadTimeText },
                    chapterKey = localCh.chapterKey.ifBlank { remoteCh.chapterKey }
                )
            } else {
                mergedMap[remoteCh.number] = remoteCh
            }
        }
        // Keep any purely local downloaded chapters whose chapter number wasn't in remoteChapters
        for (localCh in existingChapters) {
            if (!mergedMap.containsKey(localCh.number) && (localCh.localPagePaths.isNotEmpty() || localCh.isDownloaded)) {
                mergedMap[localCh.number] = localCh
            }
        }
        return mergedMap.values.sortedWith(
            compareBy<MangaChapter> { it.chapterNumberValue }
                .thenBy { it.number }
        )
    }

    // Fetch live details & chapters from the website URL whenever a valid remote URL exists
    LaunchedEffect(work.id, targetMangaUrl, refreshTrigger) {
        if (!hasValidRemoteUrl) {
            isLoadingChapters = false
            return@LaunchedEffect
        }
        if (liveWork.chapters.isEmpty() || refreshTrigger > 0) {
            isLoadingChapters = true
        }
        val res = LiveMangaSiteScraper.fetchMangaDetails(
            work = liveWork.copy(mangaUrl = targetMangaUrl),
            forceRefresh = refreshTrigger > 0
        )
        if (res.work.chapters.isNotEmpty() || res.work.description.isNotBlank()) {
            val mergedChs = mergeRemoteAndLocalChapters(res.work.chapters, liveWork.chapters)
            val updated = res.work.copy(
                localCoverPath = liveWork.localCoverPath,
                isFromLocalStorage = liveWork.isFromLocalStorage,
                storageSizeBytes = liveWork.storageSizeBytes,
                chapters = mergedChs,
                chaptersCount = maxOf(mergedChs.size, res.work.chaptersCount, liveWork.chaptersCount)
            )
            liveWork = updated
            onWorkDetailsUpdated(updated)
            requiresCloudflare = false
            isLoadingChapters = false
        } else {
            if (res.requiresCloudflare && liveWork.chapters.isEmpty()) {
                requiresCloudflare = true
            }
            isLoadingChapters = false
        }
    }

    // Background prefetch of the top/resume chapters' page URLs + first 3 page images so tapping a chapter opens in 0ms!
    LaunchedEffect(liveWork.id, liveWork.chapters.size) {
        if (liveWork.chapters.isNotEmpty()) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val candidates = listOfNotNull(
                    liveWork.chapters.firstOrNull { it.lastReadPage >= 0 },
                    liveWork.chapters.firstOrNull(),
                    liveWork.chapters.lastOrNull()
                ).distinctBy { it.chapterUrl }
                val loader = com.example.ui.components.MangaCoilImageLoader.get(detailsContext)
                candidates.take(2).forEach { targetCh ->
                    if (targetCh.chapterUrl.startsWith("http") &&
                        LiveMangaSiteScraper.getCachedChapterPages(targetCh.chapterUrl).isEmpty()
                    ) {
                        runCatching {
                            val pageRes = LiveMangaSiteScraper.fetchChapterPages(liveWork, targetCh)
                            if (pageRes.pageUrls.isNotEmpty()) {
                                LiveMangaSiteScraper.cacheChapterPages(targetCh.chapterUrl, pageRes.pageUrls)
                                pageRes.pageUrls.take(3).forEach { pageUrl ->
                                    if (pageUrl.startsWith("http")) {
                                        val req = coil.request.ImageRequest.Builder(detailsContext)
                                            .data(pageUrl)
                                            .addHeader("Referer", targetCh.chapterUrl)
                                            .addHeader("User-Agent", LiveMangaSiteScraper.activeUserAgent)
                                            .memoryCacheKey(pageUrl)
                                            .diskCacheKey(pageUrl)
                                            .build()
                                        loader.enqueue(req)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Only mount HiddenSiteWebViewBridge if OkHttp finished with 0 chapters and CloudflareProtectionDialog is NOT open
    if (hasValidRemoteUrl && !showProtectionDialog && !requiresCloudflare && !isLoadingChapters && liveWork.chapters.isEmpty()) {
        HiddenSiteWebViewBridge(
            targetUrl = targetMangaUrl,
            jsExtractorScript = LiveMangaSiteScraper.JS_EXTRACT_DETAILS,
            reloadTrigger = refreshTrigger,
            onResultJson = { rawJson ->
                val domRes = LiveMangaSiteScraper.parseDetailsFromDomJson(rawJson, liveWork)
                if (domRes.work.chapters.isNotEmpty() || domRes.work.description.isNotBlank()) {
                    val mergedChs = mergeRemoteAndLocalChapters(domRes.work.chapters, liveWork.chapters)
                    val updated = domRes.work.copy(
                        localCoverPath = liveWork.localCoverPath,
                        isFromLocalStorage = liveWork.isFromLocalStorage,
                        storageSizeBytes = liveWork.storageSizeBytes,
                        chapters = mergedChs,
                        chaptersCount = maxOf(mergedChs.size, domRes.work.chaptersCount, liveWork.chaptersCount)
                    )
                    LiveMangaSiteScraper.cacheWorkDetails(updated)
                    liveWork = updated
                    onWorkDetailsUpdated(updated)
                    requiresCloudflare = false
                    isLoadingChapters = false
                } else if (domRes.requiresCloudflare && liveWork.chapters.isEmpty()) {
                    requiresCloudflare = true
                    isLoadingChapters = false
                }
            }
        )
    }

    fun triggerActionHint(label: String) {
        scope.launch {
            bottomActionHint = label
            delay(1600)
            if (bottomActionHint == label) {
                bottomActionHint = null
            }
        }
    }

    val displayedChapters = remember(liveWork.chapters, isChaptersReversed) {
        if (isChaptersReversed) liveWork.chapters.reversed() else liveWork.chapters
    }

    val effectiveResumeChapter = remember(liveWork.chapters, readChapterNumbers) {
        liveWork.chapters.firstOrNull { it.lastReadPage >= 0 }
            ?: liveWork.chapters.firstOrNull { it.number in readChapterNumbers }
            ?: liveWork.chapters.lastOrNull()
    }
    val effectiveResumeText = remember(resumeLabel, effectiveResumeChapter) {
        resumeLabel.ifBlank { effectiveResumeChapter?.lastReadTimeText ?: "" }
    }

    val coverPalette = rememberDynamicCoverPalette(liveWork)

    // Adaptive Two-Pane Split Layout supporting both Samsung Galaxy Z Fold 4 Folded Cover Screen (الوضع المطوي) and Unfolded Main Screen (الوضع المفتوح)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundDark)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("manga_details_screen")
    ) {
        val isFoldedCompact = maxWidth < 460.dp
        val normalizedStatus = remember(liveWork.status) {
            val lower = liveWork.status.trim().lowercase()
            when {
                lower.contains("ongoing") || lower.contains("on going") || lower.contains("مستمر") -> "مستمرة"
                lower.contains("completed") || lower.contains("مكتمل") -> "مكتملة"
                lower.contains("hiatus") || lower.contains("متوقف") -> "متوقفة"
                liveWork.status.isBlank() -> "مستمرة"
                else -> liveWork.status
            }
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // ==================== RIGHT PANE: MANGA INFO & SYNOPSIS ====================
            Box(
                modifier = Modifier
                    .weight(if (isFoldedCompact) 0.44f else 0.45f)
                    .fillMaxHeight()
            ) {
                MangaCoverArtwork(
                    work = liveWork,
                    showArabicTitleBanner = false,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(26.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    coverPalette.dominantBackdropTop,
                                    coverPalette.dominantBackdropMid,
                                    coverPalette.dominantBackdropBottom
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isFoldedCompact) 6.dp else 10.dp, vertical = 6.dp)
                ) {
                    // Top Row: Back Arrow + Title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("manga_details_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = TextPrimaryWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = liveWork.title,
                            color = TextPrimaryWhite,
                            fontSize = if (isFoldedCompact) 13.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isFoldedCompact) {
                            // Folded Cover Screen (الوضع المطوي - Galaxy Z Fold 4):
                            // Stack Cover centered at top + full-width Status & Type badges so text never wraps mid-word
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                MangaCoverArtwork(
                                    work = liveWork,
                                    modifier = Modifier
                                        .width(96.dp)
                                        .height(134.dp)
                                        .border(1.dp, Color(0xFF343B4D), RoundedCornerShape(8.dp))
                                )
                            }

                            Surface(
                                color = coverPalette.badgeSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, coverPalette.badgeBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "الحالة: $normalizedStatus",
                                    color = TextPrimaryWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                                )
                            }

                            Surface(
                                color = coverPalette.badgeSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, coverPalette.badgeBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (liveWork.author.isNotBlank() && liveWork.author != "غير معروف") {
                                        "المؤلف: ${liveWork.author}"
                                    } else {
                                        "النوع: ${liveWork.type.replace("\n", " ")}"
                                    },
                                    color = TextPrimaryWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                                )
                            }
                        } else {
                            // Unfolded Main Screen (الوضع المفتوح - Galaxy Z Fold 4) & Standard Wide Display:
                            // Cover Image (Right) + Status & Type/Author Boxes (Left)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                MangaCoverArtwork(
                                    work = liveWork,
                                    modifier = Modifier
                                        .width(88.dp)
                                        .height(126.dp)
                                        .border(1.dp, coverPalette.badgeBorder, RoundedCornerShape(8.dp))
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = coverPalette.badgeSurface,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, coverPalette.badgeBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "الحالة: $normalizedStatus",
                                            color = TextPrimaryWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                                        )
                                    }

                                    Surface(
                                        color = coverPalette.badgeSurface,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, coverPalette.badgeBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (liveWork.author.isNotBlank() && liveWork.author != "غير معروف") {
                                                "المؤلف:\n${liveWork.author}"
                                            } else {
                                                "النوع:\n${liveWork.type}"
                                            },
                                            color = TextPrimaryWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 15.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                    // Rating ★ + Read 📖 + Web/Protection 🌐 Bar
                    Surface(
                        color = Color(0xD9171A22),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E3342)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    onShowToast("تقييم العمل: ${liveWork.rating}")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "التقييم",
                                    tint = Color(0xFF9EA4B8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = liveWork.rating,
                                    color = TextPrimaryWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = {
                                    val targetChapter = effectiveResumeChapter ?: liveWork.chapters.firstOrNull()
                                    if (targetChapter != null) {
                                        onOpenChapter(liveWork, targetChapter)
                                    } else {
                                        onShowToast("جاري جلب الفصول من الموقع...")
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("start_reading_icon_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = "قراءة الآن",
                                    tint = Color(0xFF9EA4B8),
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    onOpenInWebView(targetMangaUrl, liveWork.title)
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("open_web_protection_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = "فتح الموقع",
                                    tint = Color(0xFF9EA4B8),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    // Chapters Count + Source / Storage Size Row (matching 03:12 & 08:05: التخزين / ٢٨ MB | عدد الفصول 1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color(0xD9171A22),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF2E3342)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "عدد الفصول: ${liveWork.chapters.size.takeIf { it > 0 } ?: liveWork.chaptersCount}",
                                    color = TextPrimaryWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        Surface(
                            color = Color(0xD9171A22),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF2E3342)),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (liveWork.isFromLocalStorage || liveWork.storageSizeBytes > 0L) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = "التخزين",
                                        color = TextSecondaryGrey,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = MainViewModel.formatBytesArabic(liveWork.storageSizeBytes.coerceAtLeast(1024L)),
                                        color = TextPrimaryWhite,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = liveWork.siteName.substringAfterLast("- ").trim().ifBlank { liveWork.siteName },
                                        color = TextPrimaryWhite,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    SiteIconBadge(
                                        iconKey = liveWork.siteIconKey,
                                        siteName = liveWork.siteName,
                                        size = 16.dp
                                    )
                                }
                            }
                        }
                    }

                    // Genre Chips
                    if (liveWork.genres.isNotEmpty()) {
                        liveWork.genres.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                pair.forEach { genre ->
                                    Surface(
                                        color = Color(0xD9171A22),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFF3E4352)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = genre,
                                            color = TextPrimaryWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Story Synopsis
                    if (liveWork.description.isNotBlank()) {
                        Surface(
                            color = Color(0xB312151E),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = liveWork.description,
                                color = TextPrimaryWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // ==================== LEFT PANE: CHAPTERS LIST & BOTTOM ACTION BAR ====================
        Column(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
                .background(AppBackgroundDark)
        ) {
            // Top "الفصول" Tab Header + Quick Search & Multi-Select Icons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { showChapterSearchBar = !showChapterSearchBar },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("toggle_chapter_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث سريع عن رقم الفصل",
                            tint = if (showChapterSearchBar || chapterJumpQuery.isNotBlank()) PeriwinklePrimary else TextSecondaryGrey,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "الفصول",
                        color = PeriwinklePrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            isMultiSelectMode = !isMultiSelectMode
                            if (!isMultiSelectMode) {
                                selectedChapterNums = emptySet()
                            } else {
                                onShowToast("وضع التحديد المتعدد: اضغط على الفصول لتحديدها كمقروءة أو تحميلها")
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("toggle_multi_select_chapters_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "تحديد متعدد للفصول",
                            tint = if (isMultiSelectMode) PeriwinklePrimary else TextSecondaryGrey,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(PeriwinklePrimary)
                )
                HorizontalDivider(color = AppDividerColor, thickness = 1.dp)
            }

            // Exact "إستمر" Resume Reading Banner from 08:05 & 08:25:
            // "قبل 6 ثوان • الصفحة 21" + "إستمر ▶" pill button
            if (effectiveResumeChapter != null && effectiveResumeText.isNotBlank()) {
                Surface(
                    color = Color(0xFF191D28),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChapter(liveWork, effectiveResumeChapter) }
                        .testTag("details_resume_reading_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = effectiveResumeChapter.title,
                                color = TextPrimaryWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = effectiveResumeText,
                                color = TextSecondaryGrey,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                        Surface(
                            color = coverPalette.accentPrimary,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "إستمر",
                                    color = coverPalette.accentOnPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = coverPalette.accentOnPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = AppDividerColor, thickness = 1.dp)
            }

            // Chapters Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isLoadingChapters && displayedChapters.isEmpty()) {
                    // Shimmer Skeleton Loading State
                    ShimmerChaptersList(
                        itemCount = 7,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (displayedChapters.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = PeriwinklePrimary,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لم يتم استخراج الفصول تلقائياً بعد",
                            color = TextPrimaryWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { refreshTrigger++ }) {
                            Text("إعادة المحاولة", color = TextPrimaryWhite, fontSize = 12.sp)
                        }
                        if (hasValidRemoteUrl) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { showProtectionDialog = true },
                                border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.8f)),
                                modifier = Modifier.testTag("empty_chapters_verify_site_btn")
                            ) {
                                Text(
                                    text = "تخطي التحقق وعرض الفصول بالتطبيق 🛡️",
                                    color = Color(0xFFFFB74D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onOpenAlternativeSourceChapter(liveWork, 1) },
                            border = BorderStroke(1.dp, PeriwinklePrimary.copy(alpha = 0.6f)),
                            modifier = Modifier.testTag("empty_chapters_alt_source_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = PeriwinklePrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فتح من مصدر بديل", color = PeriwinklePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    val savedOfflineChapters = displayedChapters.filter { ch ->
                        val st = chapterDownloadStates[ch.number]?.status?.ifBlank { ch.downloadStatus } ?: ch.downloadStatus
                        (ch.number in downloadedChapterNumbers) || (ch.isDownloaded && st == "COMPLETED") || (ch.localPagePaths.isNotEmpty() && st == "COMPLETED")
                    }
                    val incompleteStorageChapters = if (liveWork.isFromLocalStorage) {
                        displayedChapters.filter { ch ->
                            val st = chapterDownloadStates[ch.number]?.status?.ifBlank { ch.downloadStatus } ?: ch.downloadStatus
                            val isSaved = (ch.number in downloadedChapterNumbers) || (ch.isDownloaded && st == "COMPLETED") || (ch.localPagePaths.isNotEmpty() && st == "COMPLETED")
                            !isSaved && (st == "INCOMPLETE" || st == "FAILED" || st == "DOWNLOADING" || st == "QUEUED")
                        }
                    } else {
                        emptyList()
                    }
                    val hasOnlyLocalChapters = liveWork.isFromLocalStorage &&
                        savedOfflineChapters.isNotEmpty() &&
                        (savedOfflineChapters.size + incompleteStorageChapters.size == displayedChapters.size)
                    val baseChaptersToDisplay = if (hasOnlyLocalChapters) {
                        savedOfflineChapters
                    } else {
                        displayedChapters
                    }
                    val chaptersToDisplay = remember(
                        baseChaptersToDisplay,
                        chapterFilterMode,
                        chapterJumpQuery,
                        readChapterNumbers,
                        downloadedChapterNumbers,
                        chapterDownloadStates
                    ) {
                        val modeFiltered = when (chapterFilterMode) {
                            "غير المقروءة" -> baseChaptersToDisplay.filter { ch ->
                                ch.number !in readChapterNumbers && !ch.isRead
                            }
                            "المحملة" -> baseChaptersToDisplay.filter { ch ->
                                val st = chapterDownloadStates[ch.number]?.status?.ifBlank { ch.downloadStatus } ?: ch.downloadStatus
                                (ch.number in downloadedChapterNumbers) || (ch.isDownloaded && st == "COMPLETED") || (ch.localPagePaths.isNotEmpty() && st == "COMPLETED")
                            }
                            else -> baseChaptersToDisplay
                        }
                        val cleanJump = chapterJumpQuery.trim()
                        if (cleanJump.isEmpty()) {
                            modeFiltered
                        } else {
                            modeFiltered.filter { ch ->
                                ch.number.toString().contains(cleanJump) ||
                                    ch.numberLabel.contains(cleanJump, ignoreCase = true) ||
                                    ch.title.contains(cleanJump, ignoreCase = true)
                            }
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Fast Filter Chips & Quick Chapter Jump Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("الكل", "غير المقروءة", "المحملة").forEach { filterOption ->
                                val selected = chapterFilterMode == filterOption
                                Surface(
                                    color = if (selected) PeriwinklePrimary else Color(0xFF232734),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .clickable { chapterFilterMode = filterOption }
                                        .testTag("chapter_filter_chip_$filterOption")
                                ) {
                                    Text(
                                        text = filterOption,
                                        color = if (selected) PeriwinkleOnPrimary else TextSecondaryGrey,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            if (chaptersToDisplay.size > 8) {
                                Surface(
                                    color = Color(0xFF232734),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable {
                                        scope.launch {
                                            val lastIdx = (chaptersToDisplay.size - 1).coerceAtLeast(0)
                                            val currentFirst = chaptersListState.firstVisibleItemIndex
                                            if (currentFirst < lastIdx / 2) {
                                                chaptersListState.animateScrollToItem(lastIdx)
                                            } else {
                                                chaptersListState.animateScrollToItem(0)
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "↕ انتقال سريع",
                                        color = PeriwinklePrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (showChapterSearchBar || chapterJumpQuery.isNotBlank()) {
                            OutlinedTextField(
                                value = chapterJumpQuery,
                                onValueChange = { chapterJumpQuery = it },
                                placeholder = { Text("اكتب رقم الفصل (مثلاً 120) للقفز إليه...", fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PeriwinklePrimary,
                                    unfocusedBorderColor = Color(0xFF323746),
                                    focusedTextColor = TextPrimaryWhite,
                                    unfocusedTextColor = TextPrimaryWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .testTag("chapter_jump_input")
                            )
                        }

                        // Multi-Select Batch Action Bar ("تحديد عدة فصول دفعة واحدة كمقروءة أو للتحميل")
                        if (isMultiSelectMode) {
                            Surface(
                                color = Color(0xFF1A1E2B),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, PeriwinklePrimary.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("chapters_multi_select_bar")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(
                                                color = Color(0xFF262B3C),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.clickable {
                                                    selectedChapterNums = chaptersToDisplay.map { it.number }.toSet()
                                                }
                                            ) {
                                                Text(
                                                    text = "الكل",
                                                    color = TextPrimaryWhite,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                )
                                            }
                                            Surface(
                                                color = Color(0xFF262B3C),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.clickable {
                                                    selectedChapterNums = chaptersToDisplay
                                                        .filter { it.number !in readChapterNumbers && !it.isRead }
                                                        .map { it.number }
                                                        .toSet()
                                                }
                                            ) {
                                                Text(
                                                    text = "غير المقروء",
                                                    color = TextPrimaryWhite,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                )
                                            }
                                            if (selectedChapterNums.isNotEmpty()) {
                                                Surface(
                                                    color = Color(0xFF262B3C),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.clickable { selectedChapterNums = emptySet() }
                                                ) {
                                                    Text(
                                                        text = "مسح",
                                                        color = Color(0xFFFF8A80),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "محدد: ${selectedChapterNums.size}",
                                            color = PeriwinklePrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (selectedChapterNums.isNotEmpty()) {
                                                    onMarkChaptersReadBatch(liveWork, selectedChapterNums.toList(), true)
                                                    selectedChapterNums = emptySet()
                                                    isMultiSelectMode = false
                                                } else {
                                                    onShowToast("حدد فصلاً واحداً على الأقل أولاً")
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PeriwinklePrimary,
                                                contentColor = PeriwinkleOnPrimary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(30.dp)
                                                .testTag("batch_mark_read_btn")
                                        ) {
                                            Text("تحديد كمقروء ✓", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                if (selectedChapterNums.isNotEmpty()) {
                                                    val batchChs = liveWork.chapters.filter { it.number in selectedChapterNums }
                                                    onDownloadChapters(
                                                        liveWork,
                                                        batchChs,
                                                        "بدأ تنزيل ${batchChs.size} فصل محدد..."
                                                    )
                                                    selectedChapterNums = emptySet()
                                                    isMultiSelectMode = false
                                                } else {
                                                    onShowToast("حدد فصلاً واحداً على الأقل أولاً")
                                                }
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(30.dp)
                                                .testTag("batch_download_selected_btn")
                                        ) {
                                            Text("تحميل المحدد ⬇", color = PeriwinklePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        LazyColumn(
                            state = chaptersListState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("chapters_lazy_column"),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                        if (liveWork.isFromLocalStorage && savedOfflineChapters.isNotEmpty()) {
                            item(key = "completed_offline_header") {
                                Text(
                                    text = "الفصول المكتملة (جاهزة بدون إنترنت: ${savedOfflineChapters.size} من ${displayedChapters.size})",
                                    color = Color(0xFF81C784),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        items(chaptersToDisplay, key = { "ch_${it.id}" }) { chapter ->
                            val liveChState = chapterDownloadStates[chapter.number]
                            val effectiveStatus = liveChState?.status?.ifBlank { chapter.downloadStatus } ?: chapter.downloadStatus
                            val isSaved = (chapter.number in downloadedChapterNumbers) ||
                                (chapter.localPagePaths.isNotEmpty() && effectiveStatus == "COMPLETED")
                            val isDownloading = effectiveStatus == "DOWNLOADING" || effectiveStatus == "QUEUED"
                            val isIncomplete = !isSaved && (effectiveStatus == "INCOMPLETE" || effectiveStatus == "FAILED")
                            val isRead = (chapter.number in readChapterNumbers) || chapter.isRead || (chapter.lastReadPage >= 0)
                            val isSelectedInBatch = isMultiSelectMode && (chapter.number in selectedChapterNums)
                            val showMenuForThis = activeChapterMenuId == chapter.id

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelectedInBatch) PeriwinklePrimary.copy(alpha = 0.14f) else Color.Transparent)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {
                                                if (isMultiSelectMode) {
                                                    selectedChapterNums = if (chapter.number in selectedChapterNums) {
                                                        selectedChapterNums - chapter.number
                                                    } else {
                                                        selectedChapterNums + chapter.number
                                                    }
                                                } else {
                                                    onOpenChapter(liveWork, chapter)
                                                }
                                            },
                                            onLongClick = { activeChapterMenuId = chapter.id }
                                        )
                                        .padding(horizontal = 10.dp, vertical = 10.dp)
                                        .testTag("chapter_row_${chapter.number}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Right side in RTL: Circular Chapter Number / Checkmark / Progress Badge
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSaved -> Color(0xFF1E3B2B)
                                                    isDownloading -> Color(0xFF1F273B)
                                                    isIncomplete -> Color(0xFF3E2723)
                                                    isRead -> Color(0xFF232838)
                                                    else -> Color(0xFF1A1D26)
                                                }
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = when {
                                                    isSaved -> Color(0xFF4CAF50)
                                                    isDownloading -> PeriwinklePrimary
                                                    isIncomplete -> Color(0xFFFF8A80)
                                                    isRead -> PeriwinklePrimary.copy(alpha = 0.6f)
                                                    else -> Color(0xFF3E4352)
                                                },
                                                shape = CircleShape
                                            )
                                            .clickable { activeChapterMenuId = chapter.id }
                                            .testTag("chapter_badge_${chapter.number}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when {
                                            isSaved -> {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "محفوظ",
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            isDownloading -> {
                                                CircularProgressIndicator(
                                                    color = PeriwinklePrimary,
                                                    strokeWidth = 2.dp,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            isIncomplete -> {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "غير مكتمل",
                                                    tint = Color(0xFFFF8A80),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                            else -> {
                                                Text(
                                                    text = chapter.displayNumber,
                                                    color = if (isRead) PeriwinklePrimary else TextPrimaryWhite,
                                                    fontSize = if (chapter.displayNumber.length >= 4) 10.sp else 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = chapter.title,
                                            color = if (isRead) Color(0xFFB0B8D0) else TextPrimaryWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        val subText = buildList {
                                            if (isDownloading) {
                                                val done = liveChState?.downloadedPagesCount ?: chapter.downloadedPagesCount
                                                val tot = (liveChState?.pageCount ?: chapter.pageCount).coerceAtLeast(1)
                                                add("جارٍ التنزيل ($done من $tot صفحة)")
                                            } else {
                                                if (chapter.lastReadTimeText.isNotBlank()) {
                                                    add(chapter.lastReadTimeText)
                                                } else if (chapter.dateText.isNotBlank()) {
                                                    add(chapter.dateText)
                                                }
                                                if (isSaved) add("تم التنزيل")
                                                if (isIncomplete) {
                                                    val done = liveChState?.downloadedPagesCount ?: chapter.downloadedPagesCount
                                                    val tot = (liveChState?.pageCount ?: chapter.pageCount).coerceAtLeast(1)
                                                    add("غير مكتمل ($done/$tot)")
                                                }
                                                if (isRead && chapter.lastReadTimeText.isBlank()) add("مقروء")
                                            }
                                        }.joinToString(" • ")

                                        if (subText.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = subText,
                                                color = when {
                                                    isDownloading -> PeriwinklePrimary
                                                    isIncomplete -> Color(0xFFFF8A80)
                                                    isSaved -> Color(0xFF81C784)
                                                    else -> TextSecondaryGrey
                                                },
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // 1-Tap Direct Chapter Download / Retry / Menu Button
                                    IconButton(
                                        onClick = {
                                            if (isSaved) {
                                                activeChapterMenuId = chapter.id
                                            } else {
                                                onDownloadChapters(
                                                    liveWork,
                                                    listOf(chapter),
                                                    if (isIncomplete) "جارٍ استكمال تنزيل ${chapter.title}..." else "بدأ تنزيل ${chapter.title} للقراءة بدون إنترنت..."
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("chapter_quick_download_${chapter.number}")
                                    ) {
                                        when {
                                            isSaved -> Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "محفوظ بدون إنترنت",
                                                tint = Color(0xFF81C784),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            isDownloading -> CircularProgressIndicator(
                                                color = PeriwinklePrimary,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            isIncomplete -> Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "إعادة تنزيل الفصل",
                                                tint = Color(0xFFFF8A80),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            else -> Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = "تنزيل الفصل بضغطة واحدة",
                                                tint = PeriwinklePrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                // Chapter Context Menu
                                DropdownMenu(
                                    expanded = showMenuForThis,
                                    onDismissRequest = { activeChapterMenuId = null },
                                    containerColor = Color(0xFF22252F)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (isIncomplete) "استكمال / إعادة تنزيل الفصل" else "حفظ هذا الفصل للقراءة بدون إنترنت",
                                                color = TextPrimaryWhite,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        onClick = {
                                            activeChapterMenuId = null
                                            onDownloadChapters(
                                                liveWork,
                                                listOf(chapter),
                                                if (isIncomplete) "جارٍ استكمال تنزيل ${chapter.title}..." else "بدأ التنزيل 😭"
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "تنزيل هذا الفصل والفصول السابقة",
                                                color = TextPrimaryWhite,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        onClick = {
                                            activeChapterMenuId = null
                                            val previousAndCurrent = liveWork.chapters.filter {
                                                it.chapterNumberValue <= chapter.chapterNumberValue
                                            }
                                            onDownloadChapters(
                                                liveWork,
                                                previousAndCurrent,
                                                "بدأ التنزيل 😭 (${previousAndCurrent.size} فصل)"
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "تنزيل هذا الفصل والفصول التالية",
                                                color = TextPrimaryWhite,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        onClick = {
                                            activeChapterMenuId = null
                                            val nextAndCurrent = liveWork.chapters.filter {
                                                it.chapterNumberValue >= chapter.chapterNumberValue
                                            }
                                            onDownloadChapters(
                                                liveWork,
                                                nextAndCurrent,
                                                "بدأ التنزيل 😭 (${nextAndCurrent.size} فصل)"
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "حفظ وتصدير الفصل كملف PDF",
                                                color = PeriwinklePrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        onClick = {
                                            activeChapterMenuId = null
                                            onExportChapterPdf(liveWork, chapter)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (isRead) "إلغاء تحديد الفصل كمقروء" else "تحديد هذا الفصل كمقروء ✓",
                                                color = TextPrimaryWhite,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        onClick = {
                                            activeChapterMenuId = null
                                            onMarkChaptersReadBatch(liveWork, listOf(chapter.number), !isRead)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "فتح الفصل من مصدر عربي بديل 🌐",
                                                color = PeriwinklePrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        onClick = {
                                            activeChapterMenuId = null
                                            onOpenAlternativeSourceChapter(liveWork, chapter.number)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "تصدير الفصل بصيغة .cbz",
                                                color = PeriwinklePrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        onClick = {
                                            activeChapterMenuId = null
                                            onExportChapterCbz(liveWork, chapter)
                                        }
                                    )
                                    if (isSaved || isIncomplete) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "حذف الفصل المحلي",
                                                    color = Color(0xFFFF8A80),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            },
                                            onClick = {
                                                activeChapterMenuId = null
                                                onDeleteSingleDownloadedChapter(liveWork, chapter)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Separate section for incomplete/failed chapters when opened from Storage
                        if (incompleteStorageChapters.isNotEmpty()) {
                            item(key = "incomplete_offline_header") {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color(0xFF2E3342))
                                Text(
                                    text = "فصول غير مكتملة أو قيد التنزيل (${incompleteStorageChapters.size})",
                                    color = Color(0xFFFFB74D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            items(incompleteStorageChapters, key = { "inc_${it.id}" }) { incChapter ->
                                val liveChState = chapterDownloadStates[incChapter.number]
                                val effectiveStatus = liveChState?.status?.ifBlank { incChapter.downloadStatus } ?: incChapter.downloadStatus
                                val isDownloading = effectiveStatus == "DOWNLOADING" || effectiveStatus == "QUEUED"
                                val done = liveChState?.downloadedPagesCount ?: incChapter.downloadedPagesCount
                                val tot = (liveChState?.pageCount ?: incChapter.pageCount).coerceAtLeast(1)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isDownloading) {
                                                onOpenChapter(liveWork, incChapter)
                                            } else {
                                                onDownloadChapters(
                                                    liveWork,
                                                    listOf(incChapter),
                                                    "جارٍ استكمال تنزيل ${incChapter.title}..."
                                                )
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                        .testTag("incomplete_chapter_row_${incChapter.number}"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(if (isDownloading) Color(0xFF1F273B) else Color(0xFF3E2723)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isDownloading) {
                                                CircularProgressIndicator(
                                                    color = PeriwinklePrimary,
                                                    strokeWidth = 2.dp,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "غير مكتمل",
                                                    tint = Color(0xFFFF8A80),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = incChapter.title,
                                                color = TextPrimaryWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (isDownloading) "جارٍ التنزيل ($done من $tot صفحة)" else "غير مكتمل ($done من $tot صفحة) • اضغط للاستكمال",
                                                color = if (isDownloading) PeriwinklePrimary else Color(0xFFFF8A80),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    TextButton(onClick = { onOpenChapter(liveWork, incChapter) }) {
                                        Text("فتح المتاح", color = PeriwinklePrimary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        }
                    }
                }

                // Floating Tooltip Pill above Bottom Action Bar
                androidx.compose.animation.AnimatedVisibility(
                    visible = bottomActionHint != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Surface(
                        color = Color(0xFF282C37),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 6.dp
                    ) {
                        Text(
                            text = bottomActionHint ?: "",
                            color = TextPrimaryWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Bottom 5-Icon Bar matching 00:16 - 00:38 & 08:31 - 08:32 (responsive across Z Fold 4 folded & unfolded screens)
            HorizontalDivider(color = AppDividerColor, thickness = 1.dp)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color(0xFF12151E))
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Heart Icon: أضف إلى المفضلة
                    IconButton(
                        onClick = {
                            triggerActionHint("أضف إلى المفضلة")
                            onToggleFavorite(liveWork)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("details_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "أضف إلى المفضلة",
                            tint = if (isFavorite) PeriwinklePrimary else TextPrimaryWhite,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    // 2. Reverse Chapters Icon: عكس الفصول
                    IconButton(
                        onClick = {
                            isChaptersReversed = !isChaptersReversed
                            triggerActionHint("عكس الفصول")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("details_reverse_chapters_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "عكس الفصول",
                            tint = if (isChaptersReversed) PeriwinklePrimary else TextPrimaryWhite,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    // 3. Save / Download Icon: حفظ
                    IconButton(
                        onClick = {
                            triggerActionHint("حفظ")
                            showDownloadMangaDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("details_save_manga_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "حفظ وتنزيل المانجا",
                            tint = TextPrimaryWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 4. Search in Added Sites Icon: البحث عن هذا العمل في المواقع المضافة
                    IconButton(
                        onClick = {
                            triggerActionHint("البحث عن هذا العمل في المواقع المضافة")
                            showSearchInSitesDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("details_search_other_sites_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FindReplace,
                            contentDescription = "البحث عن هذا العمل في المواقع المضافة",
                            tint = TextPrimaryWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 5. Three-dots Overflow Menu (00:30 & 08:31-08:32):
                    // تحديث الصفحة / تحديث الغلاف / انشاء اختصار / الإبلاغ / شارك / تصدير / إزالة / فتح في ...
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { showOverflowMenu = true },
                            modifier = Modifier.testTag("details_more_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "المزيد",
                                tint = TextPrimaryWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                            containerColor = Color(0xFF232630)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "تحديث الصفحة",
                                        color = TextPrimaryWhite,
                                        fontSize = 14.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    refreshTrigger++
                                    onShowToast("جاري تحديث الفصول من الموقع...")
                                }
                            )
                            if (liveWork.isFromLocalStorage) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "تحديث الغلاف",
                                            color = TextPrimaryWhite,
                                            fontSize = 14.sp,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.End
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onShowToast("تم تحديث غلاف المانجا المحلي")
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "انشاء اختصار",
                                        color = TextPrimaryWhite,
                                        fontSize = 14.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    onSaveWorkToStorage(liveWork)
                                    onShowToast("تم إنشاء اختصار للعمل في التخزين")
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "الإبلاغ",
                                        color = TextPrimaryWhite,
                                        fontSize = 14.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    onShowToast("تم إرسال بلاغ فحص الفصل")
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "شارك",
                                        color = TextPrimaryWhite,
                                        fontSize = 14.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    try {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "${liveWork.title} - $targetMangaUrl")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة العمل"))
                                    } catch (_: Exception) {
                                        onShowToast("تم نسخ رابط المشاركة")
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "فئة المكتبة ($currentCategory)",
                                        color = PeriwinklePrimary,
                                        fontSize = 14.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    showCategoryDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "نقل العمل لمصدر آخر (Migration)",
                                        color = PeriwinklePrimary,
                                        fontSize = 14.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    onFindMigrationCandidates(liveWork)
                                    showMigrationDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "تصدير الفصول بصيغة .cbz",
                                        color = TextPrimaryWhite,
                                        fontSize = 14.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    onExportAllCbz(liveWork)
                                }
                            )
                            if (liveWork.isFromLocalStorage) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "إزالة",
                                            color = Color(0xFFFF8A80),
                                            fontSize = 14.sp,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.End
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onRemoveWorkFromStorage(liveWork.id)
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "فتح في ...",
                                            color = TextPrimaryWhite,
                                            fontSize = 14.sp,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.End
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onOpenInWebView(targetMangaUrl, liveWork.title)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // Advanced Batch Download Dialog ("حفظ" button at 00:33 & 07:03 + Batch Download Options)
    if (showDownloadMangaDialog) {
        val totalCh = liveWork.chapters.size.coerceAtLeast(1)
        val sortedAsc = remember(liveWork.chapters) { liveWork.chapters.sortedBy { it.number } }
        val unreadChapters = remember(sortedAsc, readChapterNumbers) {
            sortedAsc.filter { it.number !in readChapterNumbers && !it.isRead }
        }
        AlertDialog(
            onDismissRequest = { showDownloadMangaDialog = false },
            containerColor = AppDialogSurface,
            title = {
                Text("حفظ وتنزيل الفصول", color = TextPrimaryWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "اختر طريقة التنزيل الدفعي للعمل (${liveWork.title} — $totalCh فصل):",
                        color = TextPrimaryWhite,
                        fontSize = 13.sp
                    )
                    OutlinedButton(
                        onClick = {
                            showDownloadMangaDialog = false
                            val target = unreadChapters.take(5).ifEmpty { sortedAsc.take(5) }
                            onDownloadChapters(liveWork, target, "بدأ تنزيل أول 5 فصول غير مقروءة (${target.size} فصل) 😭")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("batch_download_unread_5_btn")
                    ) {
                        Text("تنزيل أول 5 فصول غير مقروءة", color = PeriwinklePrimary, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            showDownloadMangaDialog = false
                            val target = unreadChapters.take(10).ifEmpty { sortedAsc.take(10) }
                            onDownloadChapters(liveWork, target, "بدأ تنزيل العشر فصول القادمة غير المقروءة (${target.size} فصل) 😭")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("batch_download_unread_10_btn")
                    ) {
                        Text("تنزيل العشر فصول القادمة غير المقروءة", color = PeriwinklePrimary, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            showDownloadMangaDialog = false
                            val target = unreadChapters.ifEmpty { sortedAsc }
                            onDownloadChapters(liveWork, target, "بدأ تنزيل الفصول غير المقروءة (${target.size} فصل) 😭")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("batch_download_unread_btn")
                    ) {
                        Text("تنزيل جميع الفصول غير المقروءة (${unreadChapters.size})", color = PeriwinklePrimary, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            showDownloadMangaDialog = false
                            val firstFive = sortedAsc.take(5)
                            onDownloadChapters(liveWork, firstFive, "بدأ تنزيل أول 5 فصول 😭")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("batch_download_first_5_btn")
                    ) {
                        Text("تنزيل أول 5 فصول", color = PeriwinklePrimary, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            showDownloadMangaDialog = false
                            val firstTen = sortedAsc.take(10)
                            onDownloadChapters(liveWork, firstTen, "بدأ تنزيل أول 10 فصول 😭")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("batch_download_first_10_btn")
                    ) {
                        Text("تنزيل أول 10 فصول", color = PeriwinklePrimary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            showDownloadMangaDialog = false
                            if (liveWork.chapters.isNotEmpty()) {
                                onDownloadChapters(liveWork, sortedAsc, "بدأ تنزيل جميع الفصول ($totalCh) 😭")
                            } else {
                                onSaveWorkToStorage(liveWork)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PeriwinklePrimary,
                            contentColor = PeriwinkleOnPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("details_confirm_save_all_btn")
                    ) {
                        Text("تحميل الكل ($totalCh فصل)", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDownloadMangaDialog = false }) {
                    Text("إلغاء", color = PeriwinklePrimary)
                }
            }
        )
    }

    // Custom Library Category Selector Dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            containerColor = AppDialogSurface,
            title = {
                Text("تصنيف العمل في المكتبة", color = TextPrimaryWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { catName ->
                        val isSelected = catName == currentCategory
                        Surface(
                            color = if (isSelected) PeriwinklePrimary.copy(alpha = 0.22f) else Color(0xFF232733),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isSelected) PeriwinklePrimary else Color(0xFF343A4A)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showCategoryDialog = false
                                    onAssignCategory(liveWork, catName)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = catName,
                                    color = if (isSelected) PeriwinklePrimary else TextPrimaryWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = PeriwinklePrimary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("إغلاق", color = PeriwinklePrimary)
                }
            }
        )
    }

    // Source Migration Dialog ("نقل العمل لمصدر آخر مع الاحتفاظ بسجل الفصول المقروءة")
    if (showMigrationDialog) {
        AlertDialog(
            onDismissRequest = { showMigrationDialog = false },
            containerColor = AppDialogSurface,
            title = {
                Text("نقل العمل لمصدر آخر", color = TextPrimaryWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "سيتم نقل «${liveWork.title}» إلى المصدر المختار مع الاحتفاظ بجميع الفصول المقروءة (${readChapterNumbers.size} فصل) وتقدم القراءة:",
                        color = TextSecondaryGrey,
                        fontSize = 12.sp
                    )
                    if (isSearchingMigration) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(color = PeriwinklePrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("جارٍ البحث في المصادر المفعلة...", color = TextPrimaryWhite, fontSize = 13.sp)
                        }
                    } else if (migrationCandidates.isEmpty()) {
                        Text(
                            text = "لم يتم العثور على عمل مطابق في المصادر الأخرى المفعلة حالياً.",
                            color = Color(0xFFFFB74D),
                            fontSize = 13.sp
                        )
                    } else {
                        migrationCandidates.take(6).forEach { candidate ->
                            Surface(
                                color = Color(0xFF232733),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showMigrationDialog = false
                                        onMigrateToWork(liveWork, candidate)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MangaCoverArtwork(
                                        work = candidate,
                                        modifier = Modifier
                                            .width(42.dp)
                                            .height(58.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(candidate.title, color = TextPrimaryWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        val latestCh = candidate.chapters.maxOfOrNull { it.number } ?: candidate.chaptersCount.coerceAtLeast(1)
                                        Text(
                                            text = "${candidate.siteName} • أحدث فصل: $latestCh",
                                            color = PeriwinklePrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text("نقل فوري", color = PeriwinklePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMigrationDialog = false }) {
                    Text("إغلاق", color = PeriwinklePrimary)
                }
            }
        )
    }

    // Dialog for Cross-Site Search ("البحث عن هذا العمل في المواقع المضافة")
    if (showSearchInSitesDialog) {
        AlertDialog(
            onDismissRequest = { showSearchInSitesDialog = false },
            containerColor = AppDialogSurface,
            title = {
                Text("البحث في المواقع المضافة", color = TextPrimaryWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (addedSites.isEmpty()) {
                        Text(
                            text = "لا توجد مواقع أخرى مضافة حالياً. يمكنك إضافة مواقع المانجا من قسم «إدارة المواقع».",
                            color = TextSecondaryGrey,
                            fontSize = 13.sp
                        )
                    } else {
                        Text(
                            text = "اختر موقعاً للبحث عن «${liveWork.title}» أو نقل العمل إليه:",
                            color = TextSecondaryGrey,
                            fontSize = 13.sp
                        )
                        OutlinedButton(
                            onClick = {
                                showSearchInSitesDialog = false
                                onFindMigrationCandidates(liveWork)
                                showMigrationDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("بحث تلقائي ونقل العمل مع الفصول المقروءة", color = PeriwinklePrimary, fontWeight = FontWeight.Bold)
                        }
                        addedSites.forEach { s ->
                            Surface(
                                color = Color(0xFF232733),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showSearchInSitesDialog = false
                                        onSelectSiteForSearch?.invoke(s)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SiteIconBadge(iconKey = s.iconKey, siteName = s.name, size = 28.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(s.name, color = TextPrimaryWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(s.domain, color = TextSecondaryGrey, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSearchInSitesDialog = false }) {
                    Text("إغلاق", color = PeriwinklePrimary)
                }
            }
        )
    }

    if (showProtectionDialog && hasValidRemoteUrl) {
        CloudflareProtectionDialog(
            url = targetMangaUrl,
            siteName = liveWork.siteName.ifBlank { "الموقع" },
            jsExtractorScript = LiveMangaSiteScraper.JS_EXTRACT_DETAILS,
            onExtractedDomJson = { rawJson ->
                val domRes = LiveMangaSiteScraper.parseDetailsFromDomJson(rawJson, liveWork)
                if (domRes.work.chapters.isNotEmpty() || domRes.work.description.isNotBlank()) {
                    val mergedChs = mergeRemoteAndLocalChapters(domRes.work.chapters, liveWork.chapters)
                    val updated = domRes.work.copy(
                        localCoverPath = liveWork.localCoverPath,
                        isFromLocalStorage = liveWork.isFromLocalStorage,
                        storageSizeBytes = liveWork.storageSizeBytes,
                        chapters = mergedChs,
                        chaptersCount = maxOf(mergedChs.size, domRes.work.chaptersCount, liveWork.chaptersCount)
                    )
                    LiveMangaSiteScraper.cacheWorkDetails(updated)
                    liveWork = updated
                    onWorkDetailsUpdated(updated)
                    requiresCloudflare = false
                    isLoadingChapters = false
                }
            },
            onDismissAndRefresh = {
                showProtectionDialog = false
                if (liveWork.chapters.isNotEmpty()) {
                    requiresCloudflare = false
                    isLoadingChapters = false
                    onShowToast("تم تخطي التحقق وعرض فصول المانجا داخل التطبيق")
                } else {
                    refreshTrigger++
                    onShowToast("تم تحديث جلسة التحقق، جارٍ جلب الفصول...")
                }
            }
        )
    }
}
