package bruhcollective.itaysonlab.jetispot.ui.screens.nowplaying.fullscreen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bruhcollective.itaysonlab.jetispot.core.SpPlayerServiceManager
import bruhcollective.itaysonlab.jetispot.ui.screens.nowplaying.NowPlayingViewModel
import bruhcollective.itaysonlab.jetispot.ui.shared.MarqueeText
import bruhcollective.itaysonlab.jetispot.ui.shared.PlayPauseButton

@Composable
fun NowPlayingLyricsComposition(
  viewModel: NowPlayingViewModel,
  isTextFullscreen: Boolean,
  damping: Float,
  stiffness: Float,
  lyricsClickAction: () -> Unit
) {
  val listState = rememberLazyListState()
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp +
          WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
          WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 1.dp

  LazyColumn {
    item {
      val fullscreenHeight by animateDpAsState(
        if (isTextFullscreen) screenHeight else 0.dp, spring(damping, stiffness)
      )

      LyricsCollapsed(viewModel, isTextFullscreen, damping, stiffness, lyricsClickAction)

      Column(
        Modifier
          .height(fullscreenHeight)
          .padding(horizontal = 12.dp)
          .systemBarsPadding()
          .fillMaxWidth()
      ) {
        LyricsHeader(viewModel) { lyricsClickAction() }

        Box(Modifier.weight(1f)) {
          LyricsExpanded(viewModel, Modifier.fillMaxWidth(), listState, PaddingValues(12.dp, 16.dp))

          LyricsGradientsFullscreen(listState)
        }

        LyricsControls(isTextFullscreen, viewModel, damping, stiffness)
      }
    }
  }
}

@Composable
fun LyricsGradientsFullscreen(lyricsScrollBehavior: LazyListState) {
  val gradientAlpha by animateFloatAsState(
    if (lyricsScrollBehavior.firstVisibleItemIndex != 0) 1f else 0f
  )

  Box(Modifier.fillMaxSize()) {
    Box(
      modifier = Modifier
        .fillMaxHeight(0.1f)
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            listOf(
              MaterialTheme.colorScheme.surfaceVariant.copy(gradientAlpha),
              Color.Transparent
            )
          )
        )
    )

    Box(
      modifier = Modifier
        .fillMaxHeight(0.1f)
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .background(
          Brush.verticalGradient(
            listOf(
              Color.Transparent,
              MaterialTheme.colorScheme.surfaceVariant,
            )
          )
        )
    )
  }
}

@Composable
fun LyricsControls(
  isTextFullscreen: Boolean,
  viewModel: NowPlayingViewModel,
  damping: Float, stiffness: Float
) {
  val controlsHeight by animateDpAsState(
    if (isTextFullscreen) 108.dp else 0.dp,
    spring(damping * 1.3f, stiffness)
  )

  Row(
    Modifier.height(controlsHeight).fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Surface(
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .padding(top = 8.dp, bottom = 28.dp)
        .clip(RoundedCornerShape(28.dp))
        .height(72.dp)
        .width(106.dp)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = rememberRipple(color = MaterialTheme.colorScheme.primary)
        ) { viewModel.togglePlayPause() }
    ) {
      PlayPauseButton(
        isPlaying = viewModel.currentState.value == SpPlayerServiceManager.PlaybackState.Playing,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(64.dp).align(Alignment.CenterVertically)
      )
    }
  }
}

@Composable
fun LyricsCollapsed(
  viewModel: NowPlayingViewModel,
  isTextFullscreen: Boolean,
  damping: Float,
  stiffness: Float,
  lyricsClickAction: () -> Unit
) {
  val collapsedHeight by animateDpAsState(
    if (isTextFullscreen) 0.dp else 56.dp,
    spring(damping, stiffness)
  )

  Row(
    modifier = Modifier
      .clickable(remember { MutableInteractionSource() }, null) { lyricsClickAction() }
      .height(collapsedHeight),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      Icons.Rounded.MenuBook,
      contentDescription = "",
      modifier = Modifier.padding(start = 16.dp).size(16.dp)
    )

    Column(
      Modifier.weight(1f).height(64.dp),
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = viewModel.spLyricsController.currentSongLine,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        style = TextStyle(platformStyle = PlatformTextStyle(false)),
        maxLines = 3,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 8.dp)
      )
    }

    Icon(Icons.Rounded.ExpandLess, contentDescription = "", modifier = Modifier.padding(end = 16.dp))
  }
}

@Composable
fun LyricsExpanded(
  viewModel: NowPlayingViewModel,
  modifier: Modifier,
  listState: LazyListState,
  padding: PaddingValues
) {
//  val lyricsPosition = Offset(
//    x = lerp(viewModel.lyricsCardParams.first.x, 0f),
//    y = lerp(viewModel.lyricsCardParams.first.y, 0f),
//  )
  LazyColumn(modifier, listState, padding) {
    items(viewModel.spLyricsController.currentLyricsLines) { line ->
      Text(
        text = line.words,
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

@Composable
private fun LyricsHeader(viewModel: NowPlayingViewModel, lyricsClickAction: () -> Unit) {
  Box() {
    IconButton(
      onClick = { lyricsClickAction() },
      modifier = Modifier.align(Alignment.CenterStart).padding(top = 8.dp)
    ) {
      Icon(
        Icons.Rounded.ExpandMore,
        contentDescription = "Close",
        modifier = Modifier.size(35.dp).alpha(0.7f)
      )
    }
    Column(
      Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 48.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      MarqueeText(
        viewModel.currentTrack.value.title,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        basicGradientColor = MaterialTheme.colorScheme.surfaceVariant
      )

      MarqueeText(
        viewModel.currentTrack.value.artist,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        basicGradientColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.padding(top = 2.dp).fillMaxWidth()
      )
    }
  }
}