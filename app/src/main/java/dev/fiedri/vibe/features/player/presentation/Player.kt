package dev.fiedri.vibe.features.player.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fiedri.vibe.core.ui.theme.VibeTheme
import dev.fiedri.vibe.R
import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fiedri.vibe.core.ui.composables.VibeImage
import kotlin.math.roundToLong


enum class PlayerState {
    REPEAT_ONE,
    REPEAT_ALL,
    REPEAT_OFF
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

private val MiniPlayerHeight = 92.dp



@Composable
fun Player(
    modifier: Modifier = Modifier,
    onCycleRepeat: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        val playerHeight by animateDpAsState(
            targetValue = if (uiState.isExpanded) maxHeight else MiniPlayerHeight,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "playerExpand"
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(playerHeight)
                .align(Alignment.BottomCenter),
            color = VibeTheme.colors.cards
        ) {
            AnimatedVisibility(
                visible = !uiState.isExpanded,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(120))
            ) {
                MiniPlayer(
                    currentSong = uiState.currentSong,
                    isPlaying = uiState.isPlaying,
                    currentTimeMs = uiState.currentTimeMs,
                    durationMs = uiState.durationMs,
                    onTogglePlay = {viewModel.togglePlay()},
                    onPrevious = {viewModel.previous()},
                    onNext = {viewModel.next()},
                    onSeek = {
                        ms -> viewModel.seekTo(ms)
                    },
                    onExpand = {viewModel.toggleExpansion()}
                )
            }

            AnimatedVisibility(
                visible = uiState.isExpanded,
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(120))
            ) {
                ExpandedPlayer(
                    currentSong = uiState.currentSong,
                    isPlaying = uiState.isPlaying,
                    currentTimeMs = uiState.currentTimeMs,
                    durationMs = uiState.durationMs,
                    currentSongIndex = uiState.currentSongIndex,
                    numberOfSongs = uiState.numberOfSongs,
                    isShuffle = uiState.isShuffle,
                    repeatMode = uiState.repeatMode,
                    onTogglePlay = {viewModel.togglePlay()},
                    onNext = {viewModel.next()},
                    onPrevious ={viewModel.previous()},
                    onSeek = {
                            ms -> viewModel.seekTo(ms)
                    },
                    onToggleShuffle = {viewModel.toggleShuffle()},
                    onCycleRepeat = {viewModel.alterRepeatMode()},
                    onCollapse = {viewModel.toggleExpansion()}
                )
            }
        }
    }
}

@Composable
fun MiniPlayer(
    currentSong: SongPlayingState?,
    isPlaying: Boolean,
    currentTimeMs: Long,
    durationMs: Long,
    onTogglePlay: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onExpand: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onExpand() },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        SongSeekbar(
            currentPosition = currentTimeMs,
            duration = durationMs,
            isSongLoaded = currentSong != null,
            showThumb = false,
            thumbColor = VibeTheme.colors.primary,
            activeColor = VibeTheme.colors.primary,
            restColor = Color(0xFF3F3F46),
            onSeekChanged = onSeek,
            onSeekFinished = onSeek
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Ocupa todo el espacio vertical restante
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            if (currentSong != null) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(VibeTheme.colors.muted.copy(alpha = 0.3f))
                        .border(1.dp, VibeTheme.colors.muted),
                    contentAlignment = Alignment.Center // Asegura centrado de la portada
                ) {
                    VibeImage(
                        resource = currentSong.artwork ?: R.drawable.default_cover,
                        contentDescription = "Cover",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = currentSong?.title ?: "No Song",
                    color = VibeTheme.colors.foreground,
                    fontSize = 14.sp,
                    style = VibeTheme.typography.caption,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(velocity = 20.dp)
                )
                if (currentSong != null) {
                    Text(
                        text = currentSong.artist?: "<Unknow>",
                        color = VibeTheme.colors.muted,
                        fontSize = 12.sp,
                        style = VibeTheme.typography.caption,
                        maxLines = 1
                    )
                }
            }


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                IconButton(onClick = onPrevious, shape = RectangleShape) {
                    Icon(
                        imageVector = Icons.Outlined.SkipPrevious,
                        contentDescription = "Skip to previous",
                        tint = VibeTheme.colors.foreground
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, VibeTheme.colors.foreground, shape = RectangleShape)
                        .clickable { onTogglePlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(

                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = VibeTheme.colors.foreground,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onNext, shape = RectangleShape) { // Asignado onNext
                    Icon(
                        imageVector = Icons.Outlined.SkipNext,
                        contentDescription = "Skip to next",
                        tint = VibeTheme.colors.foreground
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedPlayer(
    currentSong: SongPlayingState?,
    isPlaying: Boolean,
    currentTimeMs: Long,
    durationMs: Long,
    currentSongIndex: Int,
    numberOfSongs: Int,
    isShuffle: Boolean,
    repeatMode: PlayerState,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onCollapse: () -> Unit
) {
    val primary = VibeTheme.colors.primary
    var menuOpen by remember { mutableStateOf(false) }
    val glowColor = primary.copy(alpha = 0.5f)
    val coverFilter = remember {
        val matrix = ColorMatrix().apply { setToSaturation(0.9f) }
        val brightness = ColorMatrix().apply { setToScale(0.82f, 0.82f, 0.82f, 1f) }
        matrix.timesAssign(brightness)
        ColorFilter.colorMatrix(matrix)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VibeTheme.colors.background).statusBarsPadding()
            .drawBehind {
                val glow = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.5f, 0f),
                    radius = size.width * 0.7f
                )
                drawRect(glow)
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Artwork cuadrado (w-full aspect-square border-2 shadow glow)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .drawBehind {
                        val glowPaint = Paint().apply {
                            maskFilter = BlurMaskFilter(40.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                            color = glowColor.toArgb()
                        }
                        val spread = 12.dp.toPx()
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawRoundRect(
                                RectF(-spread, -spread, size.width + spread, size.height + spread),
                                0f,
                                0f,
                                glowPaint
                            )
                        }
                    }
            ) {
                VibeImage(
                    resource = currentSong?.artwork ?: R.drawable.default_cover,
                    contentDescription = "Cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = coverFilter

                )
            }

            // figcaption: mt-5 px-5 mb-5 flex flex-col gap-3
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = currentSong?.title ?: "No Song",
                    color = VibeTheme.colors.foreground,
                    style = VibeTheme.typography.display,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(velocity = 20.dp)
                )
                Text(
                    text = currentSong?.artist ?: "No Artist",
                    color = VibeTheme.colors.mutedForeground,
                    style = VibeTheme.typography.songArtist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentSong?.album ?: "No Album",
                    color = VibeTheme.colors.foreground,
                    style = VibeTheme.typography.caption,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(velocity = 20.dp)
                )
            }

            // Seek: px-5 flex flex-col items-center gap-2
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SongSeekbar(
                    currentPosition = currentTimeMs,
                    duration = durationMs,
                    isSongLoaded = currentSong != null,
                    showThumb = true,
                    thumbColor = primary,
                    activeColor = primary,
                    restColor = VibeTheme.colors.border,
                    onSeekChanged = onSeek,
                    onSeekFinished = onSeek
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMs(currentTimeMs),
                        color = VibeTheme.colors.mutedForeground,
                        style = VibeTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${currentSongIndex + 1}/$numberOfSongs",
                        color = VibeTheme.colors.mutedForeground,
                        style = VibeTheme.typography.caption
                    )
                    Text(
                        text = formatMs(durationMs),
                        color = VibeTheme.colors.mutedForeground,
                        style = VibeTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Controles: flex-1 + mb-20
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) primary else VibeTheme.colors.mutedForeground,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onPrevious) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .shadow(elevation = 24.dp, shape = RectangleShape)
                        .background(Color.White)
                        .clickable(onClick = onTogglePlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = VibeTheme.colors.cards,
                        modifier = Modifier
                            .size(40.dp)
                            .fillMaxSize()
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onCycleRepeat) {
                    Icon(
                        imageVector = if (repeatMode == PlayerState.REPEAT_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != PlayerState.REPEAT_OFF) primary else VibeTheme.colors.mutedForeground,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.height(80.dp))
        }

        // Header absoluto flotante (bg-card/10)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(VibeTheme.colors.cards.copy(alpha = 0.1f))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Collapse",
                    tint = VibeTheme.colors.foreground
                )
            }
            Row {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = VibeTheme.colors.foreground)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Favorite, contentDescription = "Favorite", tint = VibeTheme.colors.foreground)
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.menus_song_options),
                            tint = VibeTheme.colors.foreground
                        )
                    }
                    currentSong?.let { song ->
                        PlayerMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                            song = song
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun SongSeekbar(
    currentPosition: Long,     // ms reales (antes Float random)
    duration: Long,
    isSongLoaded: Boolean,
    showThumb: Boolean,        // false = minibar, true = fullscreen
    thumbColor: Color,
    activeColor: Color,        // el fill (primary | white según vista)
    restColor: Color,          // #3f3f46 minibar | border fullscreen
    onSeekChanged: (Long) -> Unit,
    onSeekFinished: (Long) -> Unit
) {
    var dragging by remember(currentPosition) { mutableStateOf<Long?>(null) }
    val progress = remember(currentPosition, dragging, duration) {
        val active = dragging ?: currentPosition
        if (duration > 0) active.toFloat() / duration else 0f
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (showThumb) 32.dp else 4.dp)
            .pointerInput(duration, isSongLoaded) {
                detectTapGestures { offset ->
                    if (!isSongLoaded) return@detectTapGestures
                    val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeekFinished((ratio * duration).roundToLong())
                }
            }
            .pointerInput(duration, isSongLoaded) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                        dragging = (ratio * duration).roundToLong()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                        val targetMs = (ratio * duration).roundToLong()
                        dragging = targetMs
                        onSeekChanged(targetMs)
                    },
                    onDragEnd = {
                        dragging?.let { onSeekFinished(it) }
                        dragging = null
                    }
                )
            }

    ){
        Canvas(
            modifier = Modifier.fillMaxWidth().height(4.dp).align(Alignment.TopStart)
        ){
            val trackY = size.height / 2
            val trackWidth = size.width
            val progressX = trackWidth * progress.coerceIn(0f, 1f)

            drawLine(
                color = restColor,
                start = Offset(0f, trackY),
                end = Offset(trackWidth, trackY),
                strokeWidth = 4.dp.toPx()
            )

            drawLine(
                color = activeColor,
                start = Offset(0f, trackY),
                end = Offset(progressX, trackY),
                strokeWidth = 4.dp.toPx()
            )

            if (showThumb) {
                drawRect(
                    color = thumbColor,
                    topLeft = Offset(progressX - 3.dp.toPx(), trackY - 8.dp.toPx()),
                    size = Size(6.dp.toPx(), 16.dp.toPx())
                )
            }

        }
    }
}
