package com.l3on1kl.movies.presentation.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.l3on1kl.movies.domain.usecase.GetMovieDetailsUseCase
import com.l3on1kl.movies.util.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val app: Application,
    savedStateHandle: SavedStateHandle,
    private val getMovieDetails: GetMovieDetailsUseCase
) : AndroidViewModel(app) {

    private val movieId: Long = checkNotNull(savedStateHandle["movieId"]) {
        "movieId is required"
    }

    private val _state = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val state: StateFlow<DetailsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = DetailsUiState.Loading
            getMovieDetails(movieId)
                .catch { error ->
                    _state.value = DetailsUiState.Error(
                        ErrorMapper.map(
                            error,
                            app
                        )
                    )
                }
                .collect { movie ->
                    _state.value = DetailsUiState.Success(movie)
                }
        }
    }
}
