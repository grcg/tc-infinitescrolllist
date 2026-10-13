package com.tc.infinitepokemonscrolllist.domain.repository

import com.tc.infinitepokemonscrolllist.domain.model.PokemonPage

interface PokemonRepository {
    suspend fun getInitialPage(): PokemonPage
    suspend fun getNextPage(nextUrl: String): PokemonPage
}