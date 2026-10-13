package com.tc.infinitepokemonscrolllist.domain.usecase

import com.tc.infinitepokemonscrolllist.domain.model.PokemonPage
import com.tc.infinitepokemonscrolllist.domain.repository.PokemonRepository

class GetNextPokemonPageUseCase(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(nextUrl: String): PokemonPage = repository.getNextPage(nextUrl)
}