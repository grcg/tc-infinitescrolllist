package com.tc.infinitepokemonscrolllist.data.mapper

import com.tc.infinitepokemonscrolllist.data.remote.dto.PokemonDTO
import com.tc.infinitepokemonscrolllist.data.remote.dto.PokemonListResponse
import com.tc.infinitepokemonscrolllist.domain.model.Pokemon
import com.tc.infinitepokemonscrolllist.domain.model.PokemonPage

fun PokemonDTO.toDomain(): Pokemon = Pokemon(
    name = name,
    url = url
)

fun PokemonListResponse.toDomain(): PokemonPage = PokemonPage(
    pokemons = results.map { it.toDomain() },
    nextUrl = next
)