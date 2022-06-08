package bruhcollective.itaysonlab.jetispot.ui.screens.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bruhcollective.itaysonlab.jetispot.core.SpPlayerServiceManager
import bruhcollective.itaysonlab.jetispot.ui.ext.compositeSurfaceElevation
import bruhcollective.itaysonlab.jetispot.ui.shared.PlayPauseButton
import bruhcollective.itaysonlab.jetispot.ui.shared.PreviewableSyncImage

@Composable
fun NowPlayingMiniplayer(
  viewModel: NowPlayingViewModel,
  modifier: Modifier
) {
  Surface(tonalElevation = 8.dp, modifier = modifier) {
    Box(Modifier.background(MaterialTheme.colorScheme.compositeSurfaceElevation(8.dp)).fillMaxSize()) {
      LinearProgressIndicator(
        progress = viewModel.currentPosition.value.progressRange,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
          .height(2.dp)
          .fillMaxWidth()
      )

      Row(
        Modifier
          .fillMaxHeight()
          .padding(horizontal = 16.dp)
      ) {
        PreviewableSyncImage(
          viewModel.currentTrack.value.artworkCompose,
          placeholderType = "track",
          modifier = Modifier
            .size(48.dp)
            .align(Alignment.CenterVertically)
            .clip(RoundedCornerShape(8.dp))
        )

        Column(
          Modifier
            .weight(2f)
            .padding(horizontal = 14.dp)
            .align(Alignment.CenterVertically)
        ) {
          Text(
            viewModel.currentTrack.value.title,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 16.sp
          )
          Text(
            viewModel.currentTrack.value.artist,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
          )
        }

        PlayPauseButton(
          viewModel.currentState.value == SpPlayerServiceManager.PlaybackState.Playing,
          MaterialTheme.colorScheme.onSurface,
          Modifier
            .fillMaxHeight()
            .width(56.dp)
            .align(Alignment.CenterVertically).clickable {
              viewModel.togglePlayPause()
            }
        )
      }
    }
  }
}