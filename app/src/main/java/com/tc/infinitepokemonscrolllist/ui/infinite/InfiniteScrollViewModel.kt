package com.tc.infinitepokemonscrolllist.ui.infinite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tc.infinitepokemonscrolllist.domain.repository.PokemonRepository
import com.tc.infinitepokemonscrolllist.domain.usecase.GetInitialPokemonUseCase
import com.tc.infinitepokemonscrolllist.domain.usecase.GetNextPokemonPageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InfiniteScrollViewModel(
    repository: PokemonRepository
) : ViewModel() {

    private val getInitialPokemon = GetInitialPokemonUseCase(repository)
    private val getNextPokemonPage = GetNextPokemonPageUseCase(repository)

    private val _uiState = MutableStateFlow(InfiniteScrollUIState())
    val uiState: StateFlow<InfiniteScrollUIState> = _uiState.asStateFlow()

    private var nextUrl: String? = null
    private var isFetching = false

    init {
        loadInitial()
    }

    private fun loadInitial() {
        if (isFetching) return
        isFetching = true
        _uiState.update { it.copy(isInitialLoading = true, errorMessage = null) }

        viewModelScope.launch {
            runCatching { getInitialPokemon() }
                .onSuccess { page ->
                    nextUrl = page.nextUrl
                    _uiState.update {
                        it.copy(
                            items = page.pokemons,
                            isInitialLoading = false,
                            isEndReached = page.nextUrl == null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            errorMessage = throwable.message ?: "Failed to load"
                        )
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
            runCatching { getNextPokemonPage(url) }
                .onSuccess { page ->
                    nextUrl = page.nextUrl
                    _uiState.update {
                        it.copy(
                            items = it.items + page.pokemons,
                            isAppending = false,
                            isEndReached = page.nextUrl == null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isAppending = false,
                            errorMessage = throwable.message ?: "Failed to load more"
                        )
                    }
                }
            isFetching = false
        }
    }

    fun retry() {
        if (_uiState.value.items.isEmpty()) loadInitial() else loadMore()
    }
}