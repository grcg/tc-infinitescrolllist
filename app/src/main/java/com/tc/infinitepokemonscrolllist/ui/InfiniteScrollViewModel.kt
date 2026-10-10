package com.tc.infinitepokemonscrolllist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tc.infinitepokemonscrolllist.data.NetworkModule
import com.tc.infinitepokemonscrolllist.data.PokemonDto
import com.tc.infinitepokemonscrolllist.data.PokemonRepository
import com.tc.infinitepokemonscrolllist.data.PokemonRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InfiniteScrollUiState(
    val items: List<PokemonDto> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isAppending: Boolean = false,
    val isEndReached: Boolean = false,
    val errorMessage: String? = null
)

class InfiniteScrollViewModel(
    private val repository: PokemonRepository = PokemonRepositoryImpl(NetworkModule.pokemonApi)
) : ViewModel() {

    private val _uiState = MutableStateFlow(InfiniteScrollUiState())
    val uiState: StateFlow<InfiniteScrollUiState> = _uiState.asStateFlow()

    private var nextUrl: String? = null
    private var isFetching = false

    init { loadInitial() }

    private fun loadInitial() {
        if (isFetching) return
        isFetching = true
        _uiState.update { it.copy(isInitialLoading = true, errorMessage = null) }

        viewModelScope.launch {
            runCatching { repository.getInitial() }
                .onSuccess { response ->
                    nextUrl = response.next
                    _uiState.update {
                        it.copy(
                            items = response.results,
                            isInitialLoading = false,
                            isEndReached = response.next == null
                        )
                    }
                }
                .onFailure { t ->
                    _uiState.update {
                        it.copy(isInitialLoading = false, errorMessage = t.message ?: "Failed")
                    }
                }
            isFetching = false
        }
    }

    fun loadMore() {
        val url = nextUrl ?: return
        if (isFetching) return
        isFetching = true
        _uiState.update { it.copy(isAppending = true, errorMessage = null) }

        viewModelScope.launch {
            runCatching { repository.getPage(url) }
                .onSuccess { response ->
                    nextUrl = response.next
                    _uiState.update {
                        it.copy(
                            items = it.items + response.results, // append to cache
                            isAppending = false,
                            isEndReached = response.next == null
                        )
                    }
                }
                .onFailure { t ->
                    _uiState.update {
                        it.copy(isAppending = false, errorMessage = t.message ?: "Failed")
                    }
                }
            isFetching = false
        }
    }

    fun retry() {
        if (_uiState.value.items.isEmpty()) loadInitial() else loadMore()
    }
}