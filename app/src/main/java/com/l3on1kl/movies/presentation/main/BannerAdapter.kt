package com.l3on1kl.movies.presentation.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.l3on1kl.movies.databinding.ItemBannerBinding
import com.l3on1kl.movies.domain.model.Movie

class BannerAdapter : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    private val items = mutableListOf<Movie>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(
        list: List<Movie>
    ) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = BannerViewHolder(
        ItemBannerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(
        holder: BannerViewHolder,
        position: Int
    ) = holder.bind(items[position])

    class BannerViewHolder(
        private val binding: ItemBannerBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            movie: Movie
        ) {
            Glide.with(binding.bannerImage)
                .load("https://image.tmdb.org/t/p/w780${movie.posterPath}")
                .into(binding.bannerImage)
        }
    }
}
