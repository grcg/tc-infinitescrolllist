package com.tc.infinitepokemonscrolllist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tc.infinitepokemonscrolllist.data.remote.NetworkModule
import com.tc.infinitepokemonscrolllist.data.repository.PokemonRepositoryImpl
import com.tc.infinitepokemonscrolllist.domain.repository.PokemonRepository
import com.tc.infinitepokemonscrolllist.ui.PokemonListScreen
import com.tc.infinitepokemonscrolllist.ui.infinite.InfiniteScrollViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    Scaffold { padding ->
                        val pokemonRepository: PokemonRepository by lazy {
                            PokemonRepositoryImpl(NetworkModule.pokemonApi)
                        }
                        val factory = viewModelFactory {
                            initializer {
                                InfiniteScrollViewModel(pokemonRepository)
                            }
                        }
                        val viewModel: InfiniteScrollViewModel = viewModel(factory = factory)
                        PokemonListScreen(
                            modifier = Modifier.padding(padding),
                            viewModel
                        )
                    }
                }
            }
        }
    }
}