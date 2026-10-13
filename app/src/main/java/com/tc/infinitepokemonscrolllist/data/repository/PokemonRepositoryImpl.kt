package com.tc.infinitepokemonscrolllist.data.repository

import com.tc.infinitepokemonscrolllist.data.mapper.toDomain
import com.tc.infinitepokemonscrolllist.data.remote.api.PokemonApi
import com.tc.infinitepokemonscrolllist.domain.repository.PokemonRepository

class PokemonRepositoryImpl(
    private val api: PokemonApi
) : PokemonRepository {
    override suspend fun getInitialPage() = api.getInitial().toDomain()
    override suspend fun getNextPage(nextUrl: String) = api.getPage(nextUrl).toDomain()
}