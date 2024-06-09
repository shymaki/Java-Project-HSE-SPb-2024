package navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

abstract class Refreshable {
    var isRefreshing = false

    abstract fun load()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RefreshableContent(
    refreshHelper: MutableState<out Refreshable?>,
    initialized: MutableState<Boolean>? = null,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    if (initialized?.value != true) {
        coroutineScope.launch {
            refreshHelper.value?.load()
        }
        initialized?.value = true
    }
    val onRefresh: () -> Unit = {
        coroutineScope.launch {
            refreshHelper.value?.load()
        }
    }
    DisposableEffect(onRefresh) {
        RefreshButton.onRefresh = onRefresh
        onDispose {
            RefreshButton.onRefresh = null
        }
    }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshHelper.value?.isRefreshing ?: false,
        onRefresh = {
            onRefresh()
        }
    )
    Box(
        modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)
    ) {
        content()
        PullRefreshIndicator(
            refreshing = refreshHelper.value?.isRefreshing ?: false,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
