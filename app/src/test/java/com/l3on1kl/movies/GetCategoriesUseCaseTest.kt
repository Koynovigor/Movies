package com.l3on1kl.movies

import com.l3on1kl.movies.domain.model.MovieCategory
import com.l3on1kl.movies.domain.repository.MoviesRepository
import com.l3on1kl.movies.domain.usecase.GetCategoriesUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GetCategoriesUseCaseTest {
    @Mock
    lateinit var repository: MoviesRepository

    private lateinit var useCase: GetCategoriesUseCase

    @Before
    fun setUp() {
        useCase = GetCategoriesUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository`() = runTest {
        val categories = listOf(
            MovieCategory(1, "Action")
        )

        `when`(repository.getCategories()).thenReturn(flowOf(categories))

        val result = useCase().first()

        assertEquals(categories, result)

        verify(repository).getCategories()
    }
}