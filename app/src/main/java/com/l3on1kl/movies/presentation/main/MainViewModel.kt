package com.l3on1kl.movies.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l3on1kl.movies.domain.model.MovieCategory
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
    private val getMovies: GetMoviesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        refresh()
    }

    private val categories = MovieCategory.entries
    private val pages = categories.associateWith { 1 }.toMutableMap()

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val bannerMovies = getMovies(
                    MovieCategory.POPULAR,
                    1
                ).first()

                val catStates = categories.map { cat ->
                    val movies = getMovies(
                        cat,
                        pages[cat] ?: 1
                    ).first()

                    CategoryState(cat, movies)
                }

                _state.value = UiState.Success(
                    bannerMovies.take(5),
                    catStates
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(
                    e.message ?: "Unexpected error"
                )
            }
        }
    }

    fun loadNextPage(category: MovieCategory) {
        val next = (pages[category] ?: 1) + 1
        pages[category] = next
        viewModelScope.launch {
            getMovies(
                category,
                next
            ).collect { list ->
                val current =
                    (_state.value as? UiState.Success) ?: return@collect

                val updated = current.categories.map {
                    if (it.category == category) {
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
