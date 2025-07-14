package com.l3on1kl.movies.presentation.main

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.l3on1kl.movies.databinding.ItemCategoryBinding
import com.l3on1kl.movies.domain.model.MovieCategory
import java.util.Locale

class CategoryAdapter(
    private val loadNext: (category: MovieCategory) -> Unit
) : ListAdapter<CategoryState, CategoryAdapter.CategoryViewHolder>(Diff) {

    private val scrollStates = mutableMapOf<Int, Parcelable?>()
    private val lastRequests = mutableMapOf<Int, Int>()

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
    ) {
        val item = getItem(position)
        holder.bind(item, scrollStates[item.category.id])
    }

    override fun onViewRecycled(holder: CategoryViewHolder) {
        holder.currentCategory?.let { category ->
            scrollStates[category.id] = holder.getScrollState()
        }
        super.onViewRecycled(holder)
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
        private val loadNext: (category: MovieCategory) -> Unit
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        private val adapter = MovieAdapter()
        private val layoutManager = LinearLayoutManager(
            binding.root.context,
            RecyclerView.HORIZONTAL,
            false
        )
        var currentCategory: MovieCategory? = null
            private set

        init {
            binding.moviesRecycler.layoutManager = layoutManager
            binding.moviesRecycler.adapter = adapter

            binding.moviesRecycler.itemAnimator = null

            binding.moviesRecycler.addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(
                        recyclerView: RecyclerView,
                        dx: Int,
                        dy: Int
                    ) {
                        currentCategory?.let { category ->
                            scrollStates[category.id] = layoutManager.onSaveInstanceState()

                            val lastVisible = layoutManager.findLastVisibleItemPosition()
                            val total = adapter.itemCount
                            if (lastVisible >= total - PREFETCH_THRESHOLD) {
                                val lastReq = lastRequests[category.id] ?: -1
                                if (total > lastReq) {
                                    lastRequests[category.id] = total
                                    loadNext(category)
                                }
                            }
                        }
                    }
                }
            )
        }

        fun bind(
            state: CategoryState,
            savedState: Parcelable?
        ) {
            binding.categoryTitle.text = state.category.title.replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(Locale.getDefault())
                } else it.toString()
            }
            currentCategory = state.category
            adapter.submitList(state.movies) {
                savedState?.let { layoutManager.onRestoreInstanceState(it) }
            }
        }

        fun getScrollState(): Parcelable? = layoutManager.onSaveInstanceState()
    }

    private companion object {
        const val PREFETCH_THRESHOLD = 10
    }
}
