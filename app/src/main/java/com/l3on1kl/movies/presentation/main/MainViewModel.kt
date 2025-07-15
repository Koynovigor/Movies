package com.l3on1kl.movies.presentation.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.l3on1kl.movies.domain.model.MovieCategory
import com.l3on1kl.movies.domain.usecase.GetCategoriesUseCase
import com.l3on1kl.movies.domain.usecase.GetMoviesUseCase
import com.l3on1kl.movies.util.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val app: Application,
    private val getMovies: GetMoviesUseCase,
    private val getCategories: GetCategoriesUseCase
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _snackbar = MutableSharedFlow<String>()
    val snackbar: SharedFlow<String> = _snackbar.asSharedFlow()

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

                var errorMessage: String? = null
                val catStates = categories.mapNotNull { cat ->
                    runCatching {
                        val movies = getMovies(
                            cat,
                            pages[cat.id] ?: 1
                        ).first()
                        CategoryState(cat, movies)
                    }.onFailure { e ->
                        errorMessage = ErrorMapper.map(
                            e,
                            app
                        )
                    }.getOrNull()
                }

                if (catStates.isNotEmpty()) {
                    _state.value = UiState.Success(catStates)
                    errorMessage?.let { _snackbar.emit(it) }
                } else {
                    throw java.io.IOException("No cached data")
                }
            } catch (e: Exception) {
                val message = ErrorMapper.map(e, app)

                if (_state.value is UiState.Success) {
                    _snackbar.emit(message)
                } else {
                    _state.value = UiState.Error(message)
                }
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
            ).catch { error ->
                _snackbar.emit(
                    ErrorMapper.map(error, app)
                )
            }.collect { list ->
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
