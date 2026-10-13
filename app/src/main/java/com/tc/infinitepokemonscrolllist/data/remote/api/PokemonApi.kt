package com.tc.infinitepokemonscrolllist.data.remote.api

import com.tc.infinitepokemonscrolllist.data.remote.dto.PokemonListResponse
import retrofit2.http.GET
import retrofit2.http.Url

interface PokemonApi {
    @GET("pokemon?limit=10&offset=0")
    suspend fun getInitial(): PokemonListResponse

    @GET
    suspend fun getPage(@Url url: String): PokemonListResponse
}