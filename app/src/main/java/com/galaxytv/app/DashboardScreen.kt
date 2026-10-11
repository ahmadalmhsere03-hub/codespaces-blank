package com.galaxytv.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

internal enum class GalaxySection(val titleRes: Int, val icon: ImageVector, val accent: Color) {
    Live(R.string.section_live, Icons.Default.LiveTv, Color(0xFF32AFFF)),
    Sports(R.string.section_sports, Icons.Default.SportsSoccer, Color(0xFFAB68FF)),
    Movies(R.string.section_movies, Icons.Default.Movie, Color(0xFFFF668C)),
    Series(R.string.section_series, Icons.Default.Tv, Color(0xFF46D9C3)),
    Kids(R.string.section_kids, Icons.Default.ChildCare, Color(0xFFFFC857)),
    Ramadan(R.string.section_ramadan, Icons.Default.NightsStay, Color(0xFF65A4FF)),
    Settings(R.string.section_settings, Icons.Default.Settings, Color(0xFFB7C5E8)),
}

@Composable
internal fun DashboardScreen(
    onSelect: (GalaxySection) -> Unit,
    showActivationNotice: Boolean,
    activationServerMessage: String,
    subscriptionExpiry: String,
    activationOsd: String,
    onDismissNotice: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFF090D21), Color(0xFF111432), Color(0xFF090A1B))),
        ),
    ) {
        val tileHeight = ((maxHeight - 96.dp) / 3 - 10.dp).coerceIn(72.dp, 145.dp)
        androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher),
                        contentDescription = stringResource(R.string.logo_description),
                        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("GALAXY", color = Color(0xFF8ABFFF), fontSize = 25.sp, fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DashboardTile(
                        section = GalaxySection.Live,
                        modifier = Modifier.weight(1f).height(tileHeight * 3 + 24.dp),
                        large = true,
                        onClick = { onSelect(GalaxySection.Live) },
                    )
                    DashboardTile(
                        section = GalaxySection.Sports,
                        modifier = Modifier.weight(1f).height(tileHeight * 3 + 24.dp),
                        large = true,
                        onClick = { onSelect(GalaxySection.Sports) },
                    )
                    Column(
                        modifier = Modifier.weight(1.55f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DashboardTile(GalaxySection.Movies, Modifier.weight(1f).height(tileHeight), onClick = { onSelect(GalaxySection.Movies) })
                            DashboardTile(GalaxySection.Series, Modifier.weight(1f).height(tileHeight), onClick = { onSelect(GalaxySection.Series) })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DashboardTile(GalaxySection.Kids, Modifier.weight(1f).height(tileHeight), onClick = { onSelect(GalaxySection.Kids) })
                            DashboardTile(GalaxySection.Ramadan, Modifier.weight(1f).height(tileHeight), onClick = { onSelect(GalaxySection.Ramadan) })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DashboardTile(GalaxySection.Settings, Modifier.weight(1f).height(tileHeight), onClick = { onSelect(GalaxySection.Settings) })
                            Spacer(Modifier.weight(1f).height(tileHeight))
                        }
                    }
                }
            }
        }

        if (showActivationNotice) {
            AlertDialog(
                onDismissRequest = onDismissNotice,
                title = { Text(stringResource(R.string.activation_notice_title)) },
                text = {
                    Text(
                        buildList {
                            add(activationServerMessage.ifBlank { stringResource(R.string.activation_success) })
                            if (subscriptionExpiry.isNotBlank()) {
                                add(stringResource(R.string.expiry_format, subscriptionExpiry))
                            }
                            if (activationOsd.isNotBlank()) add(activationOsd)
                        }.joinToString("\n"),
                    )
                },
                confirmButton = {
                    TextButton(onClick = onDismissNotice) { Text(stringResource(R.string.dismiss)) }
                },
            )
        }
    }
}

@Composable
private fun DashboardTile(
    section: GalaxySection,
    modifier: Modifier = Modifier,
    large: Boolean = false,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier.clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFF111831), Color(0xFF0D1027))))
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = section.accent.copy(alpha = if (focused) 1f else 0.72f),
                shape = shape,
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = section.icon,
            contentDescription = null,
            tint = section.accent,
            modifier = Modifier.size(if (large) 46.dp else 27.dp),
        )
        Spacer(Modifier.height(if (large) 12.dp else 7.dp))
        Text(
            text = stringResource(section.titleRes),
            color = Color(0xFFE3E8FF),
            fontSize = if (large) 19.sp else 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun SectionScreen(
    section: GalaxySection,
    subscriptionExpiry: String,
    onBack: () -> Unit,
    channelPackages: List<ServerPackage> = emptyList(),
    channels: List<ServerChannel> = emptyList(),
    selectedPackage: ServerPackage? = null,
    isContentLoading: Boolean = false,
    contentError: String? = null,
    onSelectPackage: (ServerPackage) -> Unit = {},
    onSelectChannel: (ServerChannel) -> Unit = {},
    onRetryContent: () -> Unit = {},
    mediaCategories: List<MediaCategory> = emptyList(),
    selectedMediaCategory: MediaCategory? = null,
    catalogItems: List<CatalogItem> = emptyList(),
    selectedSeries: CatalogItem? = null,
    episodes: List<MediaEpisode> = emptyList(),
    onSelectMediaCategory: (MediaCategory) -> Unit = {},
    onSelectCatalogItem: (CatalogItem) -> Unit = {},
    onSelectEpisode: (MediaEpisode) -> Unit = {},
    catalogSearch: String = "",
    onCatalogSearchChange: (String) -> Unit = {},
) {
    BackHandler(onBack = onBack)
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF090D21)).padding(30.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Icon(section.icon, contentDescription = null, tint = section.accent, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Text(stringResource(section.titleRes), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            when (section) {
                GalaxySection.Settings -> {
                    Surface(
                        color = Color(0xFF121832),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(stringResource(R.string.account), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.subscription_active), color = Color(0xFF9AA8C9))
                            if (subscriptionExpiry.isNotBlank()) {
                                Text(stringResource(R.string.subscription_expiry, subscriptionExpiry), color = Color(0xFF9AA8C9))
                            }
                        }
                    }
                }
                GalaxySection.Live, GalaxySection.Sports -> ChannelPackagesContent(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    section = section,
                    packages = channelPackages,
                    channels = channels,
                    selectedPackage = selectedPackage,
                    isLoading = isContentLoading,
                    errorMessage = contentError,
                    onSelectPackage = onSelectPackage,
                    onSelectChannel = onSelectChannel,
                    onRetry = onRetryContent,
                )
                GalaxySection.Movies, GalaxySection.Series, GalaxySection.Kids, GalaxySection.Ramadan -> CatalogBrowserContent(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    section = section,
                    categories = mediaCategories,
                    selectedCategory = selectedMediaCategory,
                    catalogItems = catalogItems,
                    selectedSeries = selectedSeries,
                    episodes = episodes,
                    isLoading = isContentLoading,
                    errorMessage = contentError,
                    onSelectCategory = onSelectMediaCategory,
                    onSelectItem = onSelectCatalogItem,
                    onSelectEpisode = onSelectEpisode,
                    onRetry = onRetryContent,
                    searchQuery = catalogSearch,
                    onSearchChange = onCatalogSearchChange,
                )
            }

            if (section == GalaxySection.Settings) Spacer(Modifier.weight(1f))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF377EF5)),
            ) {
                Text(stringResource(R.string.back))
            }
        }
    }
}

@Composable
private fun ChannelPackagesContent(
    modifier: Modifier,
    section: GalaxySection,
    packages: List<ServerPackage>,
    channels: List<ServerChannel>,
    selectedPackage: ServerPackage?,
    isLoading: Boolean,
    errorMessage: String?,
    onSelectPackage: (ServerPackage) -> Unit,
    onSelectChannel: (ServerChannel) -> Unit,
    onRetry: () -> Unit,
) {
    val visiblePackages = packages.filter { ActivationClient.matchesSportsPackage(it.name) == (section == GalaxySection.Sports) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            stringResource(
                when {
                    selectedPackage != null -> R.string.channels
                    section == GalaxySection.Sports -> R.string.sports_packages
                    else -> R.string.live_packages
                },
            ),
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            selectedPackage?.name ?: stringResource(R.string.sports_packages_hint),
            color = Color(0xFF9AA8C9),
        )

        when {
            isLoading -> CircularProgressIndicator(color = Color(0xFF39A7FF))
            errorMessage != null -> {
                Text(errorMessage, color = Color(0xFFFF858D))
                TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
            selectedPackage != null && channels.isEmpty() -> Text(stringResource(R.string.no_channels), color = Color(0xFF9AA8C9))
            selectedPackage == null && visiblePackages.isEmpty() -> Text(
                stringResource(if (section == GalaxySection.Sports) R.string.no_sports_packages else R.string.no_live_packages),
                color = Color(0xFF9AA8C9),
            )
            selectedPackage != null -> BilingualChannelLists(
                channels = channels,
                onSelect = onSelectChannel,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            else -> BilingualPackageLists(
                packages = visiblePackages,
                onSelect = onSelectPackage,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                emptyLabel = if (section == GalaxySection.Sports) R.string.no_sports_packages else R.string.no_live_packages,
            )
        }
    }
}

@Composable
private fun BilingualPackageLists(
    packages: List<ServerPackage>,
    onSelect: (ServerPackage) -> Unit,
    modifier: Modifier,
    emptyLabel: Int,
) {
    val englishPackages = packages.filterNot { ActivationClient.containsArabic(it.name) }
    val arabicPackages = packages.filter { ActivationClient.containsArabic(it.name) }
    androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PackageColumn(R.string.language_en, englishPackages, onSelect, Modifier.weight(1f), emptyLabel)
            VerticalDivider(color = Color(0xFF283253))
            PackageColumn(R.string.language_ar, arabicPackages, onSelect, Modifier.weight(1f), emptyLabel)
        }
    }
}

@Composable
private fun PackageColumn(
    titleRes: Int,
    packages: List<ServerPackage>,
    onSelect: (ServerPackage) -> Unit,
    modifier: Modifier,
    emptyLabel: Int,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(titleRes), color = Color(0xFF91A0C5), fontWeight = FontWeight.SemiBold)
        if (packages.isEmpty()) {
            Text(stringResource(emptyLabel), color = Color(0xFF7581A3), fontSize = 13.sp)
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(packages, key = { it.id }) { item ->
                    Surface(
                        color = Color(0xFF121832),
                        shape = RoundedCornerShape(9.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(item) },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (item.iconUrl.startsWith("http", ignoreCase = true)) {
                                AsyncImage(
                                    model = item.iconUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(5.dp)),
                                )
                                Spacer(Modifier.width(9.dp))
                            }
                            Text(item.name, color = Color.White, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BilingualChannelLists(
    channels: List<ServerChannel>,
    onSelect: (ServerChannel) -> Unit,
    modifier: Modifier,
) {
    val english = channels.filterNot { ActivationClient.containsArabic(it.name) }
    val arabic = channels.filter { ActivationClient.containsArabic(it.name) }
    androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChannelColumn(R.string.language_en, english, onSelect, Modifier.weight(1f))
            VerticalDivider(color = Color(0xFF283253))
            ChannelColumn(R.string.language_ar, arabic, onSelect, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ChannelColumn(
    titleRes: Int,
    channels: List<ServerChannel>,
    onSelect: (ServerChannel) -> Unit,
    modifier: Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(titleRes), color = Color(0xFF91A0C5), fontWeight = FontWeight.SemiBold)
        if (channels.isEmpty()) {
            Text(stringResource(R.string.no_channels), color = Color(0xFF7581A3), fontSize = 13.sp)
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(channels, key = { it.id.ifBlank { it.streamUrl } }) { channel ->
                    Surface(
                        color = Color(0xFF121832),
                        shape = RoundedCornerShape(9.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(channel) },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.LiveTv, contentDescription = null, tint = Color(0xFF39A7FF))
                            Spacer(Modifier.width(9.dp))
                            Text(channel.name, color = Color.White, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogBrowserContent(
    modifier: Modifier,
    section: GalaxySection,
    categories: List<MediaCategory>,
    selectedCategory: MediaCategory?,
    catalogItems: List<CatalogItem>,
    selectedSeries: CatalogItem?,
    episodes: List<MediaEpisode>,
    isLoading: Boolean,
    errorMessage: String?,
    onSelectCategory: (MediaCategory) -> Unit,
    onSelectItem: (CatalogItem) -> Unit,
    onSelectEpisode: (MediaEpisode) -> Unit,
    onRetry: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
) {
    val filteredCategories = categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val filteredItems = catalogItems.filter { it.title.contains(searchQuery, ignoreCase = true) }
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(Modifier.width(255.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.categories), color = Color(0xFF9EABD0), fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.search_catalog)) },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF39A7FF),
                    unfocusedBorderColor = Color(0xFF454C76),
                    focusedLabelColor = Color(0xFF39A7FF),
                    unfocusedLabelColor = Color(0xFF9BA9D3),
                    cursorColor = Color(0xFF39A7FF),
                ),
            )
            if (filteredCategories.isEmpty() && !isLoading) {
                Text(stringResource(R.string.no_categories), color = Color(0xFF7581A3), fontSize = 13.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(filteredCategories, key = { "${it.kind}-${it.id}" }) { category ->
                        val selected = category == selectedCategory
                        Surface(
                            color = if (selected) Color(0xFF202A51) else Color(0xFF11172D),
                            shape = RoundedCornerShape(7.dp),
                            modifier = Modifier.fillMaxWidth().clickable { onSelectCategory(category) },
                        ) {
                            Text(
                                category.name,
                                color = if (selected) Color.White else Color(0xFFC4CBE0),
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
            }
        }

        VerticalDivider(color = Color(0xFF283253))
        Box(Modifier.weight(1f).fillMaxHeight()) {
            when {
                isLoading -> CircularProgressIndicator(color = Color(0xFF39A7FF), modifier = Modifier.align(Alignment.Center))
                errorMessage != null -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(errorMessage, color = Color(0xFFFF858D))
                    TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                }
                categories.isEmpty() -> Text(stringResource(R.string.no_categories), color = Color(0xFF9AA8C9))
                selectedSeries != null -> {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(selectedSeries.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        if (episodes.isEmpty()) {
                            Text(stringResource(R.string.no_episodes), color = Color(0xFF9AA8C9))
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                items(episodes, key = { it.id }) { episode ->
                                    Surface(
                                        color = Color(0xFF121832),
                                        shape = RoundedCornerShape(9.dp),
                                        modifier = Modifier.fillMaxWidth().clickable { onSelectEpisode(episode) },
                                    ) {
                                        Text(episode.title, color = Color.White, modifier = Modifier.padding(13.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                filteredItems.isEmpty() -> Text(stringResource(R.string.no_catalog_items), color = Color(0xFF9AA8C9))
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(145.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        count = filteredItems.size,
                        key = { index -> "${filteredItems[index].kind}-${filteredItems[index].id}" },
                    ) { index ->
                        val item = filteredItems[index]
                        Column(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onSelectItem(item) },
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                Modifier.fillMaxWidth().aspectRatio(0.72f)
                                    .background(Color(0xFF161D35), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (item.posterUrl.startsWith("http", ignoreCase = true)) {
                                    AsyncImage(
                                        model = item.posterUrl,
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                    )
                                } else {
                                    Icon(section.icon, contentDescription = null, tint = section.accent, modifier = Modifier.size(36.dp))
                                }
                            }
                            Text(item.title, color = Color.White, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}
