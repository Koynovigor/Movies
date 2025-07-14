package com.l3on1kl.movies.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.l3on1kl.movies.R
import com.l3on1kl.movies.databinding.ItemMovieBinding
import com.l3on1kl.movies.domain.model.Movie
import com.l3on1kl.movies.util.TmdbConfigHolder
import com.l3on1kl.movies.util.toPosterUrl

class MovieAdapter :
    ListAdapter<Movie, MovieAdapter.MovieViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(
            oldItem: Movie,
            newItem: Movie
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Movie,
            newItem: Movie
        ) = oldItem == newItem
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = MovieViewHolder(
        ItemMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        holder: MovieViewHolder,
        index: Int
    ) = holder.bind(
        getItem(index)
    )

    class MovieViewHolder(
        private val itemViewBinding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(itemViewBinding.root) {

        fun bind(movie: Movie) {
            itemViewBinding.title.text = movie.title

            val url = movie.posterPath.toPosterUrl(
                TmdbConfigHolder.imagesConfig
            )
                ?: movie.backdropPath.toPosterUrl(
                    TmdbConfigHolder.imagesConfig
                )
                ?: R.drawable.ic_poster_placeholder

            Glide.with(itemViewBinding.poster)
                .load(url)
                .placeholder(R.drawable.ic_poster_placeholder)
                .error(R.drawable.ic_poster_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(itemViewBinding.poster)
        }
    }
}
