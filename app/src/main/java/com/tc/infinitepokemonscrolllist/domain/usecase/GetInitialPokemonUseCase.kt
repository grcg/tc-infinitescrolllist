package com.tc.infinitepokemonscrolllist.domain.usecase

import com.tc.infinitepokemonscrolllist.domain.model.PokemonPage
import com.tc.infinitepokemonscrolllist.domain.repository.PokemonRepository

class GetInitialPokemonUseCase(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(): PokemonPage = repository.getInitialPage()
}