package com.l3on1kl.movies.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l3on1kl.movies.domain.model.MovieCategory
import com.l3on1kl.movies.domain.usecase.GetCategoriesUseCase
import com.l3on1kl.movies.domain.usecase.GetMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getMovies: GetMoviesUseCase,
    private val getCategories: GetCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        refresh()
    }

    private var categories: List<MovieCategory> = emptyList()
    private val pages = mutableMapOf<Int, Int>()

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                categories = getCategories().first()
                pages.clear()
                categories.forEach { pages[it.id] = 1 }

                val catStates = categories.map { cat ->
                    val movies = getMovies(
                        cat,
                        pages[cat.id] ?: 1
                    ).first()

                    CategoryState(cat, movies)
                }

                _state.value = UiState.Success(
                    catStates
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(
                    e.localizedMessage ?: "Unexpected error"
                )
            }
        }
    }

    fun loadNextPage(category: MovieCategory) {
        val next = (pages[category.id] ?: 1) + 1
        pages[category.id] = next

        viewModelScope.launch {
            getMovies(
                category,
                next
            ).collect { list ->
                val current =
                    (_state.value as? UiState.Success) ?: return@collect

                val updated = current.categories.map {
                    if (it.category.id == category.id) {
                        it.copy(
                            movies = it.movies + list
                        )
                    } else it
                }
                _state.value = current.copy(
                    categories = updated
                )
            }
        }
    }
}
