package bruhcollective.itaysonlab.jetispot.ui.screens.dac

import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import bruhcollective.itaysonlab.jetispot.core.SpPlayerServiceManager
import bruhcollective.itaysonlab.jetispot.core.api.SpInternalApi
import bruhcollective.itaysonlab.jetispot.ui.LambdaNavigationController
import bruhcollective.itaysonlab.jetispot.ui.dac.DacRender
import bruhcollective.itaysonlab.jetispot.ui.dac.components_home.FilterComponentBinder
import bruhcollective.itaysonlab.jetispot.ui.ext.dynamicUnpack
import bruhcollective.itaysonlab.jetispot.ui.shared.PagingErrorPage
import bruhcollective.itaysonlab.jetispot.ui.shared.PagingLoadingPage
import bruhcollective.itaysonlab.jetispot.ui.shared.evo.LargeTopAppBar
import com.google.protobuf.Any
import com.google.protobuf.Message
import com.spotify.dac.api.components.VerticalListComponent
import com.spotify.dac.api.v1.proto.DacResponse
import com.spotify.home.dac.component.experimental.v1.proto.FilterComponent
import com.spotify.home.dac.component.v1.proto.HomePageComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// generally just a HubScreen with simplifed code and DAC arch usage
// DAC is something like another ServerSideUI from Spotify
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DacRendererScreen(
  navController: LambdaNavigationController,
  title: String,
  fullscreen: Boolean = false,
  loader: suspend SpInternalApi.(String) -> DacResponse,
  viewModel: DacViewModel = hiltViewModel()
) {
  val sbd = rememberSplineBasedDecay<Float>()
  val topBarState = rememberTopAppBarScrollState()
  val scrollBehavior = remember { if (fullscreen) TopAppBarDefaults.pinnedScrollBehavior(topBarState) else TopAppBarDefaults.exitUntilCollapsedScrollBehavior(sbd, topBarState) }
  val scope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    viewModel.load(loader)
  }

  when (viewModel.state) {
    is DacViewModel.State.Loaded -> {
      Scaffold(
        topBar = {
          if (fullscreen) {
            SmallTopAppBar(
              title = {},
              colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
              ),
              scrollBehavior = scrollBehavior
            )
        } else {
          LargeTopAppBar(
            title = { Text(title) },
            navigationIcon = {
              IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Rounded.ArrowBack, null)
              }
          },
            contentPadding = PaddingValues(top = with(LocalDensity.current) {
              WindowInsets.statusBars.getTop(
                LocalDensity.current).toDp() }
            ),
            scrollBehavior = scrollBehavior
          )
        }
      },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
      )
      { padding ->
        LazyColumn(
          modifier = Modifier
            .fillMaxHeight()
            .let { if (!fullscreen) it.padding(padding) else it }
        ) {
          (viewModel.state as? DacViewModel.State.Loaded)?.data?.apply {
            val cmBind: (List<Any>) -> Unit = { componentsList ->
              items(componentsList) { item ->
                var exception: Exception? = null
                var unpackedItem: Message? = null

                try {
                  unpackedItem = item.dynamicUnpack()
                } catch (e: Exception) {
                  exception = e
                }

                if (unpackedItem == null && exception != null) {
                  when (exception) {
                    is ClassNotFoundException -> {
                      Text("DAC unsupported component", Modifier.padding(horizontal = 16.dp))
                      Text(exception.message ?: "",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp).padding(horizontal = 16.dp)
                      )
                    }
                    else -> {
                      Text("DAC rendering error: ${exception.message}\n\n${exception.stackTraceToString()}")
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))
                } else if (unpackedItem != null) {
                  if (unpackedItem is FilterComponent) {
                    FilterComponentBinder(unpackedItem, viewModel.facet) { nf ->
                      scope.launch {
                        viewModel.facet = nf
                        viewModel.reload(loader)
                      }
                    }
                  } else {
                    DacRender(navController, unpackedItem)
                  }
                }
              }
            }

            when (this) {
              is VerticalListComponent -> cmBind(this.componentsList)
              is HomePageComponent -> cmBind(this.componentsList)
            }

            item {
              Spacer(modifier = Modifier.height(8.dp))
            }
          }
        }
      }
    }

    is DacViewModel.State.Error -> {
      PagingErrorPage(
        exception = (viewModel.state as DacViewModel.State.Error).error,
        onReload = { scope.launch { viewModel.reload(loader) } },
        modifier = Modifier.fillMaxSize()
      )
    }

    DacViewModel.State.Loading -> {
      PagingLoadingPage(Modifier.fillMaxSize())
    }
  }
}

@HiltViewModel
class DacViewModel @Inject constructor(
  private val spInternalApi: SpInternalApi,
  private val spPlayerServiceManager: SpPlayerServiceManager
) : ViewModel() {
  var facet = "default"

  private val _state = mutableStateOf<State>(State.Loading)
  val state: State get() = _state.value

  suspend fun load(loader: suspend SpInternalApi.(String) -> DacResponse) {
    _state.value = try {
      val unpackedRaw = spInternalApi.loader(facet)
      val unpackedMessage = unpackedRaw.component.dynamicUnpack()
      State.Loaded(unpackedMessage)
    } catch (e: Exception) {
      e.printStackTrace()
      State.Error(e)
    }
  }

  suspend fun reload(loader: suspend SpInternalApi.(String) -> DacResponse) {
    _state.value = State.Loading
    load(loader)
  }

  sealed class State {
    class Loaded(val data: Message) : State()
    class Error(val error: Exception) : State()
    object Loading : State()
  }
}
