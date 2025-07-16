package com.l3on1kl.movies.presentation.details

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.chip.Chip
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.l3on1kl.movies.R
import com.l3on1kl.movies.databinding.FragmentMovieDetailsBinding
import com.l3on1kl.movies.domain.model.MovieDetails
import com.l3on1kl.movies.util.NetworkMonitor
import com.l3on1kl.movies.util.TmdbConfigHolder
import com.l3on1kl.movies.util.toPosterUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MovieDetailsFragment : Fragment() {
    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel by viewModels<MovieDetailsViewModel>()
    private val args by navArgs<MovieDetailsFragmentArgs>()

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragmentContainer
            scrimColor = SurfaceColors.SURFACE_2.getColor(requireContext())
        }
        postponeEnterTransition()
    }

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

        ViewCompat.setTransitionName(
            binding.backdrop,
            "poster_${args.movieId}"
        )

        binding.backChip.setOnClickListener {
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkMonitor.isOnline.drop(1).collect { online ->
                    if (online) viewModel.load()
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
        scrollContent.visibility = View.GONE
        scrollContent.visibility = View.VISIBLE
        ratingChip.visibility = View.GONE
        backdrop.visibility = View.VISIBLE
        backdrop.setImageResource(R.drawable.ic_poster_placeholder)
        showPlaceholders()
        startPostponedEnterTransition()
    }

    private fun setError(
        message: String
    ) = with(binding) {
        progressBar.visibility = View.GONE
        scrollContent.visibility = View.VISIBLE
        ratingChip.visibility = View.GONE
        backdrop.visibility = View.VISIBLE
        backdrop.setImageResource(R.drawable.ic_poster_placeholder)
        showPlaceholders()
        startPostponedEnterTransition()

        Snackbar.make(
            root,
            message,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.retry) {
            viewModel.load()
        }.show()
    }

    private fun setData(
        movie: MovieDetails
    ) = with(binding) {
        progressBar.visibility = View.GONE
        scrollContent.visibility = View.VISIBLE
        ratingChip.visibility = View.VISIBLE
        backdrop.visibility = View.VISIBLE

        title.text = movie.title
        tagline.text = movie.tagline
        overview.text = movie.overview
        clearPlaceholders()

        val releaseDate = movie.releaseDate.let {
            try {
                val parser = DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd",
                    Locale.US
                )

                val date = LocalDate.parse(
                    it,
                    parser
                )

                val formatter = DateTimeFormatter.ofPattern(
                    "d MMM yyyy",
                    Locale.forLanguageTag("ru")
                )

                date.format(formatter)
            } catch (_: Exception) {
                ""
            }
        }

        infoGroup.removeAllViews()

        val infoList = listOfNotNull(
            releaseDate,
            movie.runtime?.let {
                getString(
                    R.string.minutes_format,
                    it
                )
            }
        )

        infoList.forEach { text ->
            val chip = Chip(requireContext()).apply {
                this.text = text
                isClickable = false
                isCheckable = false
                setChipBackgroundColorResource(R.color.chipBackgroundColor)
            }
            infoGroup.addView(chip)
        }

        val genresInfo = movie.genres.map { genre ->
            genre.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault())
                else char.toString()
            }
        }

        genresInfo.forEach { text ->
            val chip = Chip(requireContext()).apply {
                this.text = text
                isClickable = false
                isCheckable = false
                setChipBackgroundColorResource(R.color.chipBackgroundColor)
            }
            genresGroup.addView(chip)
        }

        originalTitle.text = movie.originalTitle?.let {
            getString(
                R.string.original_title_format,
                it
            )
        } ?: getString(R.string.empty)

        status.text = movie.status?.let {
            getString(
                R.string.status_format,
                it
            )
        } ?: getString(R.string.empty)

        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

        budget.text = movie.budget?.let {
            getString(
                R.string.budget_format,
                numberFormat.format(it)
            )
        } ?: getString(R.string.empty)

        revenue.text = movie.revenue?.let {
            getString(
                R.string.revenue_format,
                numberFormat.format(it)
            )
        } ?: getString(R.string.empty)

        ratingChip.text = String.format(
            Locale.US,
            "%.1f",
            movie.voteAverage
        )

        val url = movie.backdropPath.toPosterUrl(TmdbConfigHolder.imagesConfig)
            ?: movie.posterPath.toPosterUrl(TmdbConfigHolder.imagesConfig)
            ?: R.drawable.ic_poster_placeholder

        Glide.with(backdrop)
            .load(url)
            .placeholder(R.drawable.ic_poster_placeholder)
            .error(R.drawable.ic_poster_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    (resource as? BitmapDrawable)?.toBitmap()?.let { bmp ->
                        androidx.palette.graphics.Palette.from(bmp).generate { palette ->
                            val color = palette?.getDominantColor(
                                SurfaceColors.SURFACE_2.getColor(requireContext())
                            ) ?: SurfaceColors.SURFACE_2.getColor(requireContext())
                            collapsingToolbar.setContentScrimColor(color)
                        }
                    }
                    startPostponedEnterTransition()
                    return false
                }
            })
            .centerCrop()
            .into(backdrop)
    }

    private fun showPlaceholders() = with(binding) {
        val textViews = listOf(
            title,
            tagline,
            overview,
            originalTitle,
            status,
            budget,
            revenue
        )

        val verticalSpacing = resources.getDimensionPixelSize(
            R.dimen.placeholder_vertical_spacing
        )

        textViews.forEach { view ->
            view.text = ""
            view.setBackgroundResource(R.drawable.placeholder_rect)
            view.layoutParams.height =
                resources.getDimensionPixelSize(R.dimen.placeholder_height)
            (view.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                lp.bottomMargin = verticalSpacing
                view.layoutParams = lp
            }
        }

        infoGroup.removeAllViews()
        genresGroup.removeAllViews()

        repeat(2) {
            infoGroup.addView(createPlaceholderChip())
            genresGroup.addView(createPlaceholderChip())
        }
    }

    private fun createPlaceholderChip(): Chip = Chip(requireContext()).apply {
        text = ""
        isClickable = false
        isCheckable = false
        setChipBackgroundColorResource(R.color.colorOutline)

        val width = resources.getDimensionPixelSize(
            R.dimen.placeholder_chip_width
        )

        val height = resources.getDimensionPixelSize(
            R.dimen.placeholder_chip_height
        )

        layoutParams = ViewGroup.LayoutParams(width, height)
    }

    private fun clearPlaceholders() = with(binding) {
        val textViews = listOf(
            title,
            tagline,
            overview,
            originalTitle,
            status,
            budget,
            revenue
        )
        textViews.forEach { view ->
            view.background = null
            view.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            (view.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                lp.bottomMargin = 0
                view.layoutParams = lp
            }
        }
        infoGroup.removeAllViews()
        genresGroup.removeAllViews()
    }
}
