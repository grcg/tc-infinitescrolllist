package com.tc.infinitepokemonscrolllist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun <T> InfiniteScrollList(
    items: List<T>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    isInitialLoading: Boolean = false,
    isAppending: Boolean = false,
    isEndReached: Boolean = false,
    errorMessage: String? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    threshold: Int = 3,
    onLoadMore: () -> Unit = {},
    onRetry: () -> Unit = {},
    key: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit
) {
    LaunchedEffect(listState, items.size, isEndReached, isAppending, errorMessage) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 1 - threshold
        }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                if (!isEndReached && !isAppending && !isInitialLoading && errorMessage == null) {
                    onLoadMore()
                }
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isInitialLoading && items.isEmpty() ->
                CircularProgressIndicator(Modifier.align(Alignment.Center))

            errorMessage != null && items.isEmpty() ->
                ErrorState(errorMessage, onRetry, Modifier.align(Alignment.Center))

            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    count = items.size,
                    key = if (key != null) { i -> key(items[i]) } else null
                ) { i -> itemContent(items[i]) }

                if (isAppending) item("__append__") {
                    Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                if (errorMessage != null && items.isNotEmpty()) item("__err__") {
                    ErrorState(errorMessage, onRetry, Modifier.fillMaxWidth().padding(16.dp))
                }

                if (isEndReached && items.isNotEmpty()) item("__end__") {
                    Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                        Text("End of list")
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message)
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}