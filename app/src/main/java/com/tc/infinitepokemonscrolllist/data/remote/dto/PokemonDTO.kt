package com.tc.infinitepokemonscrolllist.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDTO(
    @SerialName("name") val name: String,
    @SerialName("url") val url: String
)