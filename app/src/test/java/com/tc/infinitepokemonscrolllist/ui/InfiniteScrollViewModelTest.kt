package com.tc.infinitepokemonscrolllist.ui

import com.tc.infinitepokemonscrolllist.data.PokemonDto
import com.tc.infinitepokemonscrolllist.data.PokemonListResponse
import com.tc.infinitepokemonscrolllist.data.PokemonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        private val initialResponse: () -> PokemonListResponse,
        private val pageResponse: (String) -> PokemonListResponse
    ) : PokemonRepository {
        var initialCalls = 0
        var pageCalls = 0

        override suspend fun getInitial(): PokemonListResponse {
            initialCalls++
            return initialResponse()
        }

        override suspend fun getPage(nextUrl: String): PokemonListResponse {
            pageCalls++
            return pageResponse(nextUrl)
        }
    }

    private fun pokemon(name: String) = PokemonDto(name = name, url = "url-$name")

    private fun response(
        names: List<String>,
        next: String? = null
    ) = PokemonListResponse(
        count = names.size,
        next = next,
        previous = null,
        results = names.map(::pokemon)
    )

    @Test
    fun `initial load populates items and clears loading flag`() = runTest(dispatcher) {
        val repo = FakePokemonRepository(
            initialResponse = { response(listOf("bulbasaur", "ivysaur"), next = "page2") },
            pageResponse = { error("should not be called") }
        )

        val vm = InfiniteScrollViewModel(repo)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.items.size)
        assertEquals("bulbasaur", state.items[0].name)
        assertEquals("ivysaur", state.items[1].name)
        assertFalse(state.isInitialLoading)
        assertFalse(state.isAppending)
        assertFalse(state.isEndReached)
        assertNull(state.errorMessage)
        assertEquals(1, repo.initialCalls)
    }

    @Test
    fun `loadMore appends items and marks end reached when next is null`() = runTest(dispatcher) {
        val repo = FakePokemonRepository(
            initialResponse = { response(listOf("bulbasaur"), next = "page2") },
            pageResponse = { response(listOf("ivysaur", "venusaur"), next = null) }
        )

        val vm = InfiniteScrollViewModel(repo)
        advanceUntilIdle()

        vm.loadMore()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(3, state.items.size)
        assertEquals("bulbasaur", state.items[0].name)
        assertEquals("ivysaur", state.items[1].name)
        assertEquals("venusaur", state.items[2].name)
        assertTrue(state.isEndReached)
        assertFalse(state.isAppending)
        assertEquals(1, repo.pageCalls)
    }

    @Test
    fun `loadMore is a no-op once end is reached`() = runTest(dispatcher) {
        val repo = FakePokemonRepository(
            initialResponse = { response(listOf("bulbasaur"), next = null) },
            pageResponse = { error("should not be called") }
        )

        val vm = InfiniteScrollViewModel(repo)
        advanceUntilIdle()

        vm.loadMore()
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.items.size)
        assertEquals(0, repo.pageCalls)
    }

    @Test
    fun `initial load failure surfaces error message`() = runTest(dispatcher) {
        val repo = FakePokemonRepository(
            initialResponse = { throw RuntimeException("boom") },
            pageResponse = { error("should not be called") }
        )

        val vm = InfiniteScrollViewModel(repo)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.items.isEmpty())
        assertEquals("boom", state.errorMessage)
        assertFalse(state.isInitialLoading)
    }

    @Test
    fun `retry re-fetches initial page when items are empty`() = runTest(dispatcher) {
        var shouldFail = true
        val repo = FakePokemonRepository(
            initialResponse = {
                if (shouldFail) throw RuntimeException("network")
                response(listOf("bulbasaur"), next = null)
            },
            pageResponse = { error("should not be called") }
        )

        val vm = InfiniteScrollViewModel(repo)
        advanceUntilIdle()
        assertEquals("network", vm.uiState.value.errorMessage)

        shouldFail = false
        vm.retry()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("bulbasaur", state.items[0].name)
        assertNull(state.errorMessage)
        assertEquals(2, repo.initialCalls)
    }
}