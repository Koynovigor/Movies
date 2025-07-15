package com.l3on1kl.movies.presentation.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.l3on1kl.movies.R
import com.l3on1kl.movies.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    private val binding by lazy {
        FragmentMainBinding.bind(requireView())
    }
    private val viewModel by viewModels<MainViewModel>()
    private val categoryAdapter = CategoryAdapter(
        loadNext = { category ->
            viewModel.loadNextPage(category)
        },
        onMovieClick = { movie ->
            val action = MainFragmentDirections.actionMainToDetails(movie.id)
            findNavController().navigate(action)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerView.adapter = categoryAdapter
        binding.recyclerView.itemAnimator = null

        binding.adView.loadAd(AdRequest.Builder().build())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.state.collect { state ->
                    when (state) {
                        UiState.Loading -> setLoading()

                        is UiState.Success -> setData(state)

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
        adView.visibility = View.GONE
    }

    private fun setData(
        state: UiState.Success
    ) = with(binding) {
        progressBar.visibility = View.GONE
        errorGroup.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        adView.visibility = View.VISIBLE
        categoryAdapter.submitList(state.categories)
    }

    private fun setError(
        errorMessage: String
    ) = with(binding) {
        progressBar.visibility = View.GONE
        errorGroup.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        adView.visibility = View.GONE
        errorText.text = errorMessage

        Log.e("MainFragment", errorMessage)

        retryButton.setOnClickListener {
            viewModel.refresh()
        }
    }
}
