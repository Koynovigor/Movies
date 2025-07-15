package com.l3on1kl.movies.presentation.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.l3on1kl.movies.R
import com.l3on1kl.movies.databinding.FragmentMovieDetailsBinding
import com.l3on1kl.movies.util.TmdbConfigHolder
import com.l3on1kl.movies.util.toPosterUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class MovieDetailsFragment : Fragment() {
    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel by viewModels<MovieDetailsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailsBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        DetailsUiState.Loading -> setLoading()

                        is DetailsUiState.Error -> setError(state.message)

                        is DetailsUiState.Success -> setData(state.movie)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setLoading() = with(binding) {
        progressBar.visibility = View.VISIBLE
        contentGroup.visibility = View.GONE
        errorGroup.visibility = View.GONE
    }

    private fun setError(message: String) = with(binding) {
        progressBar.visibility = View.GONE
        contentGroup.visibility = View.GONE
        errorGroup.visibility = View.VISIBLE
        errorText.text = message
        retryButton.setOnClickListener {
            viewModel.load()
        }
    }

    private fun setData(movie: com.l3on1kl.movies.domain.model.MovieDetails) = with(binding) {
        progressBar.visibility = View.GONE
        contentGroup.visibility = View.VISIBLE
        errorGroup.visibility = View.GONE

        title.text = movie.title
        tagline.text = movie.tagline
        overview.text = movie.overview
        info.text = listOfNotNull(
            movie.releaseDate,
            movie.runtime?.let {
                getString(
                    R.string.minutes_format,
                    it
                )
            },
            movie.genres.joinToString()
        ).joinToString(separator = " \u2022 ")

        additionalInfo.text = listOfNotNull(
            movie.originalTitle?.let {
                getString(
                    R.string.original_title_format,
                    it
                )
            },
            movie.status?.let {
                getString(
                    R.string.status_format,
                    it
                )
            },
            movie.budget?.let {
                getString(
                    R.string.budget_format,
                    it
                )
            },
            movie.revenue?.let {
                getString(
                    R.string.revenue_format,
                    it
                )
            },
            movie.originalLanguage
        ).joinToString(separator = "\n")

        rating.text = String.format(Locale.US, "%.1f", movie.voteAverage)

        val url =
            movie.backdropPath.toPosterUrl(TmdbConfigHolder.imagesConfig)
                ?: movie.posterPath.toPosterUrl(TmdbConfigHolder.imagesConfig)
                ?: R.drawable.ic_poster_placeholder

        Glide.with(poster)
            .load(url)
            .placeholder(R.drawable.ic_poster_placeholder)
            .error(R.drawable.ic_poster_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(poster)
    }
}
