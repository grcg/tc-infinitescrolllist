package com.tc.infinitepokemonscrolllist.domain.model

data class PokemonPage(
    val pokemons: List<Pokemon>,
    val nextUrl: String?
)