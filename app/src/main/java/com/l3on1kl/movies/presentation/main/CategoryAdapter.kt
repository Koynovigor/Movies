package com.l3on1kl.movies.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.l3on1kl.movies.databinding.ItemCategoryBinding
import com.l3on1kl.movies.domain.model.MovieCategory

class CategoryAdapter(
    private val loadNext: (category: MovieCategory) -> Unit
) : ListAdapter<CategoryState, CategoryAdapter.CategoryViewHolder>(Diff) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(
        position: Int
    ) = getItem(position).category.id.toLong()

    object Diff : DiffUtil.ItemCallback<CategoryState>() {
        override fun areItemsTheSame(
            oldItem: CategoryState,
            newItem: CategoryState
        ) = oldItem.category.id == newItem.category.id

        override fun areContentsTheSame(
            oldItem: CategoryState,
            newItem: CategoryState
        ) = oldItem == newItem
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = CategoryViewHolder(
        ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ),
        loadNext
    )

    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int
    ) = holder.bind(getItem(position))

    class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
        private val loadNext: (category: MovieCategory) -> Unit
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        private val adapter = MovieAdapter()
        private var currentCategory: MovieCategory? = null

        init {
            binding.moviesRecycler.layoutManager =
                LinearLayoutManager(
                    binding.root.context,
                    RecyclerView.HORIZONTAL,
                    false
                )
            binding.moviesRecycler.adapter = adapter

            binding.moviesRecycler.addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(
                        recyclerView: RecyclerView,
                        dx: Int,
                        dy: Int
                    ) {
                        if (!recyclerView.canScrollHorizontally(1)) {
                            currentCategory?.let(loadNext)
                        }
                    }
                }
            )
        }

        fun bind(
            state: CategoryState
        ) {
            binding.categoryTitle.text = state.category.title
            currentCategory = state.category
            adapter.submitList(state.movies)
        }
    }
}
