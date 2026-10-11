package com.galaxytv.app

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
internal fun GalaxyTvRoot() {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("galaxytv", Context.MODE_PRIVATE)
    }
    val scope = rememberCoroutineScope()

    var splashFinished by rememberSaveable { mutableStateOf(false) }
    var activationCode by remember(context) { mutableStateOf(ActivationCodeVault.load(context)) }
    var isCheckingSavedCode by remember(context) { mutableStateOf(activationCode != null) }
    var isActivating by rememberSaveable { mutableStateOf(false) }
    var activationMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var activationServerMessage by rememberSaveable { mutableStateOf("") }
    var showActivationNotice by rememberSaveable { mutableStateOf(false) }
    var activationOsd by rememberSaveable { mutableStateOf("") }
    var selectedSection by rememberSaveable { mutableStateOf<GalaxySection?>(null) }
    var catalogSearch by rememberSaveable { mutableStateOf("") }
    var subscriptionExpiry by rememberSaveable {
        mutableStateOf(preferences.getString("subscription_expiry", "").orEmpty())
    }
    var streamUserAgent by remember {
        mutableStateOf(preferences.getString("user_agent", "").orEmpty())
    }
    var channelPackages by remember { mutableStateOf(emptyList<ServerPackage>()) }
    var channelPackagesLoaded by remember { mutableStateOf(false) }
    var selectedPackage by remember { mutableStateOf<ServerPackage?>(null) }
    var channels by remember { mutableStateOf(emptyList<ServerChannel>()) }
    var channelsLoadedFor by remember { mutableStateOf<String?>(null) }
    var mediaCategories by remember { mutableStateOf(emptyList<MediaCategory>()) }
    var catalogItems by remember { mutableStateOf(emptyList<CatalogItem>()) }
    var selectedMediaCategory by remember { mutableStateOf<MediaCategory?>(null) }
    var catalogItemsLoadedFor by remember { mutableStateOf<String?>(null) }
    var catalogLoadedFor by remember { mutableStateOf<GalaxySection?>(null) }
    var selectedSeries by remember { mutableStateOf<CatalogItem?>(null) }
    var seriesEpisodes by remember { mutableStateOf(emptyList<MediaEpisode>()) }
    var selectedStream by remember { mutableStateOf<PlayableStream?>(null) }
    var isLoadingContent by remember { mutableStateOf(false) }
    var contentError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        delay(1_200)
        splashFinished = true
    }

    LaunchedEffect(splashFinished, activationCode) {
        val savedCode = activationCode
        if (!splashFinished || savedCode == null || !isCheckingSavedCode) return@LaunchedEffect

        try {
            val result = ActivationClient.activate(context.applicationContext, savedCode)
            if (result.status == 100) {
                subscriptionExpiry = result.expire
                streamUserAgent = result.userAgent.ifBlank {
                    streamUserAgent.ifBlank { defaultPlaybackUserAgent() }
                }
                activationServerMessage = result.message
                activationOsd = result.osd
                showActivationNotice = true
                preferences.edit()
                    .putString("subscription_expiry", result.expire)
                    .putString("user_agent", streamUserAgent)
                    .apply()
            } else {
                ActivationCodeVault.clear(context.applicationContext)
                preferences.edit()
                    .remove("subscription_expiry")
                    .remove("user_agent")
                    .apply()
                activationMessage = activationMessageForStatus(context, result.status, result.message)
                activationCode = null
            }
        } catch (_: Exception) {
            activationMessage = context.getString(R.string.network_error)
            activationCode = null
        } finally {
            isCheckingSavedCode = false
        }
    }

    val loadChannelPackages: (GalaxySection, Boolean) -> Unit = { section, force ->
        val code = activationCode
        if (code != null && !isLoadingContent) {
            isLoadingContent = true
            contentError = null
            scope.launch {
                try {
                    val packages = if (force || !channelPackagesLoaded) {
                        ActivationClient.fetchPackages(context.applicationContext, code)
                    } else {
                        channelPackages
                    }
                    channelPackages = packages
                    channelPackagesLoaded = true

                    val firstPackage = packages.firstOrNull { item ->
                        ActivationClient.matchesSportsPackage(item.name) == (section == GalaxySection.Sports)
                    }
                    selectedPackage = null
                    if (firstPackage != null) {
                        val firstChannels = if (channelsLoadedFor == firstPackage.id) {
                            channels
                        } else {
                            ActivationClient.fetchChannels(context.applicationContext, code, firstPackage.id)
                                .also {
                                    channels = it
                                    channelsLoadedFor = firstPackage.id
                                }
                        }
                        selectedStream = firstChannels.firstOrNull()?.let {
                            PlayableStream(it.name, it.streamUrl)
                        }
                    } else {
                        contentError = context.getString(
                            if (section == GalaxySection.Sports) R.string.no_sports_packages else R.string.no_live_packages,
                        )
                    }
                } catch (_: Exception) {
                    contentError = context.getString(R.string.content_error)
                } finally {
                    isLoadingContent = false
                }
            }
        }
    }

    val loadCatalogItems: (MediaCategory, Boolean) -> Unit = { category, force ->
        val code = activationCode
        val categoryKey = "${category.kind}:${category.id}"
        selectedMediaCategory = category
        selectedSeries = null
        seriesEpisodes = emptyList()
        if (code != null && !isLoadingContent && (force || catalogItemsLoadedFor != categoryKey)) {
            isLoadingContent = true
            contentError = null
            scope.launch {
                try {
                    catalogItems = ActivationClient.fetchCatalogItems(context.applicationContext, code, category)
                    catalogItemsLoadedFor = categoryKey
                } catch (_: Exception) {
                    contentError = context.getString(R.string.content_error)
                } finally {
                    isLoadingContent = false
                }
            }
        }
    }

    val loadCatalogCategories: (GalaxySection, Boolean) -> Unit = { section, force ->
        val code = activationCode
        if (code != null && !isLoadingContent && (force || catalogLoadedFor != section)) {
            isLoadingContent = true
            contentError = null
            catalogItems = emptyList()
            catalogItemsLoadedFor = null
            selectedSeries = null
            seriesEpisodes = emptyList()
            scope.launch {
                try {
                    val kinds = when (section) {
                        GalaxySection.Movies -> listOf(MediaKind.Movie)
                        GalaxySection.Series -> listOf(MediaKind.Series)
                        GalaxySection.Kids, GalaxySection.Ramadan -> listOf(MediaKind.Movie, MediaKind.Series)
                        else -> emptyList()
                    }
                    val fetchedCategories = kinds.flatMap {
                        ActivationClient.fetchCategories(context.applicationContext, code, it)
                    }
                    mediaCategories = when (section) {
                        GalaxySection.Kids, GalaxySection.Ramadan -> fetchedCategories.filter {
                            ActivationClient.matchesFamilyCategory(it.name, section)
                        }
                        else -> fetchedCategories
                    }
                    catalogLoadedFor = section
                    selectedMediaCategory = mediaCategories.firstOrNull()
                    selectedMediaCategory?.let { category ->
                        catalogItems = ActivationClient.fetchCatalogItems(context.applicationContext, code, category)
                        catalogItemsLoadedFor = "${category.kind}:${category.id}"
                    }
                } catch (_: Exception) {
                    contentError = context.getString(R.string.content_error)
                } finally {
                    isLoadingContent = false
                }
            }
        }
    }

    val loadSeriesEpisodes: (CatalogItem, Boolean) -> Unit = { series, _ ->
        val code = activationCode
        selectedSeries = series
        seriesEpisodes = emptyList()
        if (code != null && !isLoadingContent) {
            isLoadingContent = true
            contentError = null
            scope.launch {
                try {
                    seriesEpisodes = ActivationClient.fetchSeriesEpisodes(context.applicationContext, code, series.id)
                } catch (_: Exception) {
                    contentError = context.getString(R.string.content_error)
                } finally {
                    isLoadingContent = false
                }
            }
        }
    }

    val loadPackageChannels: (ServerPackage, Boolean) -> Unit = { item, force ->
        val code = activationCode
        if (code != null && !isLoadingContent) {
            selectedPackage = item
            if (force || channelsLoadedFor != item.id) {
                channels = emptyList()
                isLoadingContent = true
                contentError = null
                scope.launch {
                    try {
                        channels = ActivationClient.fetchChannels(context.applicationContext, code, item.id)
                        channelsLoadedFor = item.id
                    } catch (_: Exception) {
                        contentError = context.getString(R.string.content_error)
                    } finally {
                        isLoadingContent = false
                    }
                }
            }
        }
    }

    Surface(color = Color(0xFF080B1C), modifier = Modifier.fillMaxSize()) {
        when {
            !splashFinished || isCheckingSavedCode -> StartupSplashScreen(Modifier.fillMaxSize())
            activationCode == null -> ActivationScreen(
                isLoading = isActivating,
                message = activationMessage,
                onActivate = { code ->
                    scope.launch {
                        isActivating = true
                        activationMessage = null
                        try {
                            val result = ActivationClient.activate(context.applicationContext, code)
                            if (result.status == 100) {
                                ActivationCodeVault.save(context.applicationContext, code)
                                activationCode = code
                                isCheckingSavedCode = false
                                val userAgent = result.userAgent.ifBlank {
                                    "REV ${Build.MODEL.orEmpty()} ${Build.VERSION.RELEASE.orEmpty()}"
                                }
                                preferences.edit()
                                    .putString("subscription_expiry", result.expire)
                                    .putString("user_agent", userAgent)
                                    .apply()
                                subscriptionExpiry = result.expire
                                streamUserAgent = userAgent
                                activationServerMessage = result.message
                                activationOsd = result.osd
                                showActivationNotice = true
                            } else {
                                activationMessage = activationMessageForStatus(context, result.status, result.message)
                            }
                        } catch (_: Exception) {
                            activationMessage = context.getString(R.string.network_error)
                        } finally {
                            isActivating = false
                        }
                    }
                },
            )
            selectedStream != null -> ExoPlayerScreen(
                stream = selectedStream!!,
                userAgent = streamUserAgent,
                onBack = { selectedStream = null },
            )
            selectedSection == null -> DashboardScreen(
                onSelect = { section ->
                    selectedSection = section
                    selectedPackage = null
                    selectedSeries = null
                    catalogSearch = ""
                    when (section) {
                        GalaxySection.Live, GalaxySection.Sports -> loadChannelPackages(section, false)
                        GalaxySection.Movies, GalaxySection.Series, GalaxySection.Kids, GalaxySection.Ramadan ->
                            loadCatalogCategories(section, false)
                        GalaxySection.Settings -> Unit
                    }
                },
                showActivationNotice = showActivationNotice,
                activationServerMessage = activationServerMessage,
                subscriptionExpiry = subscriptionExpiry,
                activationOsd = activationOsd,
                onDismissNotice = { showActivationNotice = false },
            )
            else -> SectionScreen(
                section = selectedSection!!,
                subscriptionExpiry = subscriptionExpiry,
                channelPackages = channelPackages,
                channels = channels,
                selectedPackage = selectedPackage,
                isContentLoading = isLoadingContent,
                contentError = contentError,
                onSelectPackage = { loadPackageChannels(it, false) },
                onSelectChannel = { selectedStream = PlayableStream(it.name, it.streamUrl) },
                onRetryContent = {
                    when (selectedSection) {
                        GalaxySection.Live, GalaxySection.Sports -> loadChannelPackages(selectedSection!!, true)
                        GalaxySection.Movies, GalaxySection.Series, GalaxySection.Kids, GalaxySection.Ramadan -> {
                            when {
                                selectedSeries != null -> loadSeriesEpisodes(selectedSeries!!, true)
                                selectedMediaCategory != null -> loadCatalogItems(selectedMediaCategory!!, true)
                                else -> loadCatalogCategories(selectedSection!!, true)
                            }
                        }
                        else -> Unit
                    }
                },
                mediaCategories = mediaCategories,
                selectedMediaCategory = selectedMediaCategory,
                catalogItems = catalogItems,
                selectedSeries = selectedSeries,
                episodes = seriesEpisodes,
                catalogSearch = catalogSearch,
                onCatalogSearchChange = { catalogSearch = it },
                onSelectMediaCategory = { loadCatalogItems(it, false) },
                onSelectCatalogItem = { item ->
                    if (item.kind == MediaKind.Movie) {
                        if (item.streamUrl.startsWith("http", ignoreCase = true)) {
                            selectedStream = PlayableStream(item.title, item.streamUrl)
                        } else {
                            contentError = context.getString(R.string.content_error)
                        }
                    } else {
                        loadSeriesEpisodes(item, false)
                    }
                },
                onSelectEpisode = { episode ->
                    selectedStream = PlayableStream(episode.title, episode.streamUrl)
                },
                onBack = {
                    when {
                        selectedSeries != null -> {
                            selectedSeries = null
                            seriesEpisodes = emptyList()
                            contentError = null
                        }
                        selectedPackage != null -> {
                            selectedPackage = null
                            contentError = null
                        }
                        else -> selectedSection = null
                    }
                },
            )
        }
    }
}

private fun activationMessageForStatus(context: Context, status: Int, serverMessage: String): String =
    when (status) {
        102 -> context.getString(R.string.activation_error_suspended)
        103 -> context.getString(R.string.activation_error_not_found)
        104 -> context.getString(R.string.activation_error_expired)
        else -> serverMessage.ifBlank { context.getString(R.string.activation_error_generic) }
    }

private fun defaultPlaybackUserAgent(): String =
    "REV ${Build.MODEL.orEmpty()} ${Build.VERSION.RELEASE.orEmpty()}"
