package com.tc.infinitepokemonscrolllist.data

interface PokemonRepository {
    suspend fun getInitial(): PokemonListResponse
    suspend fun getPage(nextUrl: String): PokemonListResponse
}

class PokemonRepositoryImpl(
    private val api: PokemonApi
) : PokemonRepository {
    override suspend fun getInitial() = api.getInitial()
    override suspend fun getPage(nextUrl: String) = api.getPage(nextUrl)
}