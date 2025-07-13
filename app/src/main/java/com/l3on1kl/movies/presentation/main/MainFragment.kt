package com.l3on1kl.movies.presentation.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.l3on1kl.movies.R
import com.l3on1kl.movies.databinding.FragmentMainBinding
import com.l3on1kl.movies.domain.model.Movie
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    private val binding by lazy {
        FragmentMainBinding.bind(requireView())
    }
    private val viewModel by viewModels<MainViewModel>()
    private val adapter = MovieAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.layoutManager = LinearLayoutManager(
            requireContext()
        )
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.state.collect { state ->
                    when (state) {
                        UiState.Loading -> setLoading()

                        is UiState.Success -> setData(state.movies)

                        is UiState.Error -> setError(state.message)
                    }
                }
            }
        }
    }

    private fun setLoading() = with(binding) {
        progressBar.visibility = View.VISIBLE
        errorGroup.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    private fun setData(
        movies: List<Movie>
    ) = with(binding) {
        progressBar.visibility = View.GONE
        errorGroup.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        adapter.submitList(movies)
    }

    private fun setError(
        errorMessage: String
    ) = with(binding) {
        progressBar.visibility = View.GONE
        errorGroup.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        errorText.text = errorMessage

        Log.e("MainFragment", errorMessage)

        retryButton.setOnClickListener {
            viewModel.refresh()
        }
    }
}
