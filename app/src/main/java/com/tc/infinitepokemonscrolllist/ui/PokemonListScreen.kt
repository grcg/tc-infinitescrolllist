package com.tc.infinitepokemonscrolllist.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tc.infinitepokemonscrolllist.ui.infinite.InfiniteScrollViewModel

@Composable
fun PokemonListScreen(
    modifier: Modifier = Modifier,
    viewModel: InfiniteScrollViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    InfiniteScrollList(
        items = state.items,
        listState = rememberLazyListState(),
        isInitialLoading = state.isInitialLoading,
        isAppending = state.isAppending,
        isEndReached = state.isEndReached,
        errorMessage = state.errorMessage,
        onLoadMore = viewModel::loadMore,
        onRetry = viewModel::retry,
        key = { it.url },
        modifier = modifier.fillMaxSize()
    ) { pokemon ->
        Text(
            pokemon.name,
            modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)
        )
        HorizontalDivider()
    }
}