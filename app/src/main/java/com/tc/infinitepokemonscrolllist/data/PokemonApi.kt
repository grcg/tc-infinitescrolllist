package com.tc.infinitepokemonscrolllist.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Url

@Serializable
data class PokemonDto(
    @SerialName("name") val name: String,
    @SerialName("url") val url: String
)

@Serializable
data class PokemonListResponse(
    @SerialName("count") val count: Int,
    @SerialName("next") val next: String? = null,
    @SerialName("previous") val previous: String? = null,
    @SerialName("results") val results: List<PokemonDto> = emptyList()
)

interface PokemonApi {
    @GET("pokemon?limit=10&offset=0")
    suspend fun getInitial(): PokemonListResponse

    @GET
    suspend fun getPage(@Url url: String): PokemonListResponse
}