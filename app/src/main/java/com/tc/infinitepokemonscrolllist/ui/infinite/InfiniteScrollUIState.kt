package com.tc.infinitepokemonscrolllist.ui.infinite

import com.tc.infinitepokemonscrolllist.domain.model.Pokemon

data class InfiniteScrollUIState(
    val items: List<Pokemon> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isAppending: Boolean = false,
    val isEndReached: Boolean = false,
    val errorMessage: String? = null
)