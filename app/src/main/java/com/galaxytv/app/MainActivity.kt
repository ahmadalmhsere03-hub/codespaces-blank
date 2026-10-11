package com.galaxytv.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import java.util.Locale

private val Ink = Color(0xFF0B1118)
private val Panel = Color(0xFF141D25)
private val Mint = Color(0xFF20D9B5)
private val Muted = Color(0xFF9AA8B2)

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(Locale.ENGLISH)
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(11, 17, 24)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    GalaxyTvRoot()
                }
            }
        }
    }
}

private data class TvProgram(
    val title: String,
    val category: String,
    val detail: String,
    val image: String,
)

private data class TvChannel(val title: String, val detail: String, val tint: Color)

private val programs = listOf(
    TvProgram("ما وراء المجرّة", "فلك", "رحلة بين النجوم والسُدم البعيدة", "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?w=900&q=85"),
    TvProgram("كوكبنا الأزرق", "وثائقي", "حكايات من أعماق المحيط", "https://images.unsplash.com/photo-1518837695005-2083093ee35b?w=900&q=85"),
    TvProgram("قمم مجهولة", "مغامرة", "استكشف أماكن لم ترها من قبل", "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=900&q=85"),
    TvProgram("ليالي الصحراء", "وثائقي", "سماء صافية ومشاهد آسرة", "https://images.unsplash.com/photo-1509316785289-025f5b846b35?w=900&q=85"),
)

private val channels = listOf(
    TvChannel("Galaxy Live", "بث مباشر · علوم", Color(0xFF174C55)),
    TvChannel("رحلات", "بث مباشر · استكشاف", Color(0xFF715332)),
    TvChannel("فضاء", "بث مباشر · فلك", Color(0xFF343F79)),
    TvChannel("طبيعة", "بث مباشر · وثائقي", Color(0xFF315B45)),
)

private enum class AppTab(val title: String) {
    Home("الرئيسية"), Live("مباشر"), Saved("قائمتي"), Profile("حسابي")
}

@Composable
private fun GalaxyTvApp() {
    var introFinished by rememberSaveable { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(AppTab.Home) }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var search by remember { mutableStateOf("") }
    var savedTitles by remember { mutableStateOf(setOf<String>()) }
    var playingProgram by remember { mutableStateOf<TvProgram?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    val visiblePrograms = programs.filter { program ->
        (selectedCategory == "الكل" || program.category == selectedCategory) &&
            program.title.contains(search.trim(), ignoreCase = true)
    }

    Surface(color = Ink, modifier = Modifier.fillMaxSize()) {
        if (!introFinished) {
            StartupSplashScreen(Modifier.fillMaxSize())
        } else {
            androidx.compose.material3.Scaffold(
                containerColor = Ink,
            bottomBar = {
                if (playingProgram == null) {
                    NavigationBar(
                        containerColor = Color(0xFF101820),
                        contentColor = Color.White,
                        modifier = Modifier.navigationBarsPadding(),
                    ) {
                        AppTab.entries.forEach { tab ->
                            val icon = when (tab) {
                                AppTab.Home -> Icons.Default.Home
                                AppTab.Live -> Icons.Default.LiveTv
                                AppTab.Saved -> Icons.Default.BookmarkBorder
                                AppTab.Profile -> Icons.Default.PersonOutline
                            }
                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                icon = { Icon(icon, contentDescription = null) },
                                label = { Text(tab.title) },
                                alwaysShowLabel = true,
                            )
                        }
                    }
                }
            },
        ) { padding ->
            if (playingProgram != null) {
                PlayerScreen(
                    program = playingProgram!!,
                    isPlaying = isPlaying,
                    onTogglePlay = { isPlaying = !isPlaying },
                    onBack = { playingProgram = null },
                    modifier = Modifier.padding(padding),
                )
            } else {
                when (selectedTab) {
                    AppTab.Home -> HomeScreen(
                        modifier = Modifier.padding(padding),
                        search = search,
                        onSearchChange = { search = it },
                        selectedCategory = selectedCategory,
                        onCategoryChange = { selectedCategory = it },
                        savedTitles = savedTitles,
                        onToggleSaved = { title ->
                            savedTitles = if (title in savedTitles) savedTitles - title else savedTitles + title
                        },
                        visiblePrograms = visiblePrograms,
                        onPlay = {
                            playingProgram = it
                            isPlaying = false
                        },
                    )
                    AppTab.Live -> LiveScreen(
                        modifier = Modifier.padding(padding),
                        onPlay = {
                            playingProgram = programs.first()
                            isPlaying = false
                        },
                    )
                    AppTab.Saved -> SavedScreen(
                        modifier = Modifier.padding(padding),
                        savedPrograms = programs.filter { it.title in savedTitles },
                        onPlay = {
                            playingProgram = it
                            isPlaying = false
                        },
                    )
                    AppTab.Profile -> ProfileScreen(Modifier.padding(padding))
                }
            }
        }
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier,
    search: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    savedTitles: Set<String>,
    onToggleSaved: (String) -> Unit,
    visiblePrograms: List<TvProgram>,
    onPlay: (TvProgram) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(top = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tv, contentDescription = null, tint = Mint, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(9.dp))
                Text("Galaxy", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("TV", color = Mint, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("مساحتك للمشاهدة", color = Muted, fontSize = 14.sp)
            }
            SearchField(value = search, onValueChange = onSearchChange)
        }

        FeatureBanner(
            program = programs.first(),
            isSaved = programs.first().title in savedTitles,
            onPlay = { onPlay(programs.first()) },
            onToggleSaved = { onToggleSaved(programs.first().title) },
            modifier = Modifier.padding(horizontal = 28.dp),
        )

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeading("اكتشف حسب مزاجك", "اختر ما تحب")
            CategoryRail(selected = selectedCategory, onSelect = onCategoryChange)
        }

        if (visiblePrograms.isNotEmpty()) {
            ProgramRail(
                title = if (search.isBlank()) "اختيارات GalaxyTV" else "نتائج البحث",
                programs = visiblePrograms,
                savedTitles = savedTitles,
                onPlay = onPlay,
                onToggleSaved = onToggleSaved,
            )
        } else {
            Text(
                "لا توجد نتائج مطابقة",
                color = Muted,
                modifier = Modifier.padding(horizontal = 28.dp),
            )
        }

        LiveRail(onPlay = { onPlay(programs.first()) })
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Panel).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, contentDescription = "بحث", tint = Muted)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text("ابحث عن برنامج أو قناة", color = Muted)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FeatureBanner(
    program: TvProgram,
    isSaved: Boolean,
    onPlay: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val bannerHeight = if (maxWidth >= 700.dp) 390.dp else 330.dp
        Box(
            modifier = Modifier.fillMaxWidth().height(bannerHeight).clip(RoundedCornerShape(20.dp)),
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?w=1800&q=90",
                contentDescription = "سماء مليئة بالنجوم",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0x180B1118), Color(0xF20B1118))),
                ),
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("اختيار هذا الأسبوع", color = Mint, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(program.title, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text(
                    program.detail,
                    color = Color(0xFFD5DEE2),
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onPlay,
                        colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink),
                        shape = RoundedCornerShape(11.dp),
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(5.dp))
                        Text("شاهد الآن", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onToggleSaved,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(11.dp),
                    ) {
                        Icon(if (isSaved) Icons.Default.Check else Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(Modifier.width(5.dp))
                        Text(if (isSaved) "أُضيفت" else "أضف لقائمتي")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, color = Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CategoryRail(selected: String, onSelect: (String) -> Unit) {
    val categories = listOf("الكل", "فلك", "وثائقي", "مغامرة")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 28.dp),
    ) {
        items(categories) { category ->
            val active = category == selected
            Surface(
                color = if (active) Mint else Panel,
                contentColor = if (active) Ink else Color.White,
                shape = RoundedCornerShape(50),
                modifier = Modifier.clickable { onSelect(category) }.focusable(),
            ) {
                Text(
                    category,
                    fontSize = 14.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 11.dp),
                )
            }
        }
    }
}

@Composable
private fun ProgramRail(
    title: String,
    programs: List<TvProgram>,
    savedTitles: Set<String>,
    onPlay: (TvProgram) -> Unit,
    onToggleSaved: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeading(title, "قصص جديدة تستحق وقتك")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 28.dp),
        ) {
            items(programs, key = { it.title }) { program ->
                ProgramCard(
                    program = program,
                    isSaved = program.title in savedTitles,
                    onClick = { onPlay(program) },
                    onToggleSaved = { onToggleSaved(program.title) },
                )
            }
        }
    }
}

@Composable
private fun ProgramCard(
    program: TvProgram,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleSaved: () -> Unit,
) {
    Column(
        modifier = Modifier.width(238.dp).clip(RoundedCornerShape(13.dp)).clickable(onClick = onClick).focusable(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box {
            AsyncImage(
                model = program.image,
                contentDescription = program.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(13.dp)),
            )
            IconButton(
                onClick = onToggleSaved,
                modifier = Modifier.align(Alignment.TopStart).padding(7.dp).size(38.dp).background(Ink.copy(alpha = 0.72f), CircleShape),
            ) {
                Icon(
                    if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (isSaved) "إزالة من قائمتي" else "إضافة إلى قائمتي",
                    tint = if (isSaved) Mint else Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(program.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1)
        Text(program.detail, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun LiveRail(onPlay: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeading("على الهواء الآن", "قنوات تجريبية")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 28.dp),
        ) {
            items(channels) { channel ->
                Row(
                    modifier = Modifier.width(250.dp).clip(RoundedCornerShape(13.dp)).background(Panel)
                        .clickable(onClick = onPlay).focusable().padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(channel.tint),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(channel.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(channel.detail, color = Muted, fontSize = 12.sp)
                    }
                    Box(Modifier.size(8.dp).background(Color(0xFFFF655F), CircleShape))
                }
            }
        }
    }
}

@Composable
private fun LiveScreen(modifier: Modifier, onPlay: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        SectionHeading("البث المباشر", "اختر قناة للمتابعة")
        channels.forEach { channel ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).clip(RoundedCornerShape(14.dp))
                    .background(Panel).clickable(onClick = onPlay).focusable().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(Modifier.size(54.dp).clip(RoundedCornerShape(14.dp)).background(channel.tint), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LiveTv, contentDescription = null, tint = Color.White)
                }
                Column(Modifier.weight(1f)) {
                    Text(channel.title, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(channel.detail, color = Muted, fontSize = 13.sp)
                }
                Text("مباشر", color = Color(0xFFFF817A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SavedScreen(modifier: Modifier, savedPrograms: List<TvProgram>, onPlay: (TvProgram) -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SectionHeading("قائمتي", "برامجك المحفوظة")
        if (savedPrograms.isEmpty()) {
            Text("لم تضف أي برنامج بعد", color = Muted, modifier = Modifier.padding(horizontal = 28.dp))
        } else {
            savedPrograms.forEach { program ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).clip(RoundedCornerShape(13.dp))
                        .background(Panel).clickable { onPlay(program) }.focusable().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AsyncImage(
                        model = program.image,
                        contentDescription = program.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(132.dp).aspectRatio(16f / 9f).clip(RoundedCornerShape(9.dp)),
                    )
                    Column {
                        Text(program.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(program.category, color = Muted, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("حسابك", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("GalaxyTV · الإصدار 1.0", color = Muted)
        Text("واجهة تجريبية. أضف مصادر المحتوى المرخّصة لتفعيل المشاهدة.", color = Muted)
    }
}

@Composable
private fun PlayerScreen(
    program: TvProgram,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = program.image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.42f,
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x99000000), Color(0xDD000000)))))
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(20.dp).size(48.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
        }
        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            Icon(Icons.Default.Tv, contentDescription = null, tint = Mint, modifier = Modifier.size(52.dp))
            Text(program.title, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Text("مشغل تجريبي", color = Muted, fontSize = 15.sp)
            Text("سيظهر البث عند إضافة مصدر مرخّص للقناة", color = Color(0xFFD5DEE2), fontSize = 14.sp)
            Button(
                onClick = onTogglePlay,
                colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink),
                shape = CircleShape,
            ) {
                Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(7.dp))
                Text(if (isPlaying) "إيقاف مؤقت" else "تشغيل")
            }
        }
    }
}
