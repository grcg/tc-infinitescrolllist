package com.tc.infinitepokemonscrolllist.ui.infinite

import com.tc.infinitepokemonscrolllist.domain.model.Pokemon
import com.tc.infinitepokemonscrolllist.domain.model.PokemonPage
import com.tc.infinitepokemonscrolllist.domain.repository.PokemonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InfiniteScrollViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakePokemonRepository(
        private val initial: () -> PokemonPage,
        private val next: (String) -> PokemonPage
    ) : PokemonRepository {
        var initialCalls = 0
        var nextCalls = 0

        override suspend fun getInitialPage(): PokemonPage {
            initialCalls++
            return initial()
        }

        override suspend fun getNextPage(nextUrl: String): PokemonPage {
            nextCalls++
            return next(nextUrl)
        }
    }

    private fun pokemon(name: String) = Pokemon(name = name, url = "url-$name")

    private fun page(names: List<String>, next: String? = null) = PokemonPage(
        pokemons = names.map(::pokemon),
        nextUrl = next
    )

    @Test
    fun `initial load populates items and clears loading flag`() = runTest(dispatcher) {
        val repo = FakePokemonRepository(
            initial = { page(listOf("bulbasaur", "ivysaur"), next = "page2") },
            next = { error("unexpected") }
        )

        val vm = InfiniteScrollViewModel(repo)
        advanceUntilIdle()

        val state = vm.uiState.value
        Assert.assertEquals(2, state.items.size)
        Assert.assertEquals("bulbasaur", state.items[0].name)
        Assert.assertEquals("ivysaur", state.items[1].name)
        Assert.assertFalse(state.isInitialLoading)
        Assert.assertFalse(state.isAppending)
        Assert.assertFalse(state.isEndReached)
        Assert.assertNull(state.errorMessage)
        Assert.assertEquals(1, repo.initialCalls)
    }

    @Test
    fun `loadMore appends items and marks end reached when next is null`() = runTest(dispatcher) {
        val repo = FakePokemonRepository(
            initial = { page(listOf("bulbasaur"), next = "page2") },
            next = { page(listOf("ivysaur", "venusaur"), next = null) }
        )

        val vm = InfiniteScrollViewModel(repo)
        advanceUntilIdle()

        vm.loadMore()
        advanceUntilIdle()

        val state = vm.uiState.value
        Assert.assertEquals(3, state.items.size)
        Assert.assertEquals("bulbasaur", state.items[0].name)
        Assert.assertEquals("ivysaur", state.items[1].name)
        Assert.assertEquals("venusaur", state.items[2].name)
        Assert.assertTrue(state.isEndReached)
        Assert.assertFalse(state.isAppending)
        Assert.assertEquals(1, repo.nextCalls)
    }

    @Test
    fun `loadMore is a no-op once end is reached`() = runTest(dispatcher) {
        val repo = FakePokemonRepository(
            initial = { page(listOf("bulbasaur"), next = null) },
            next = { error("unexpected") }
        )

        val vm = InfiniteScrollViewModel(repo)
        advanceUntilIdle()

        vm.loadMore()
        advanceUntilIdle()

        Assert.assertEquals(1, vm.uiState.value.items.size)
        Assert.assertEquals(0, repo.nextCalls)
    }

    @Test
    fun `initial load failure surfaces error message`() = runTest(dispatcher) {
        val repo = FakePokemonRepository(
            initial = { throw RuntimeException("boom") },
            next = { error("unexpected") }
        )

        val vm = InfiniteScrollViewModel(repo)
        advanceUntilIdle()

        val state = vm.uiState.value
        Assert.assertTrue(state.items.isEmpty())
        Assert.assertEquals("boom", state.errorMessage)
        Assert.assertFalse(state.isInitialLoading)
    }

    @Test
    fun `retry re-fetches initial page when items are empty`() = runTest(dispatcher) {
        var shouldFail = true
        val repo = FakePokemonRepository(
            initial = {
                if (shouldFail) throw RuntimeException("network")
                page(listOf("bulbasaur"), next = null)
            },
            next = { error("unexpected") }
        )

        val vm = InfiniteScrollViewModel(repo)
        advanceUntilIdle()
        Assert.assertEquals("network", vm.uiState.value.errorMessage)

        shouldFail = false
        vm.retry()
        advanceUntilIdle()

        val state = vm.uiState.value
        Assert.assertEquals(1, state.items.size)
        Assert.assertEquals("bulbasaur", state.items[0].name)
        Assert.assertNull(state.errorMessage)
        Assert.assertEquals(2, repo.initialCalls)
    }
}