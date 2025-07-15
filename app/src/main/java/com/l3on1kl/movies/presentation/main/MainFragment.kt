package com.l3on1kl.movies.presentation.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.l3on1kl.movies.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel by activityViewModels<MainViewModel>()

    private val categoryAdapter = CategoryAdapter(
        loadNext = { category ->
            viewModel.loadNextPage(category)
        },
        onMovieClick = { movie ->
            val action = MainFragmentDirections.actionMainToDetails(movie.id)
            findNavController().navigate(action)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
