package com.l3on1kl.movies.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.material.snackbar.Snackbar
import com.l3on1kl.movies.R
import com.l3on1kl.movies.databinding.FragmentMainBinding
import com.l3on1kl.movies.util.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel by activityViewModels<MainViewModel>()
    private var snackbar: Snackbar? = null

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    private val categoryAdapter = CategoryAdapter(
        loadNext = { category ->
            viewModel.loadNextPage(category)
        },
        onMovieClick = { movie ->
            val action = MainFragmentDirections.actionMainToDetails(movie.id)
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.mainFragment) {
                navController.navigate(action)
            }
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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.snackbar.collect { message ->
                    setError(message)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkMonitor.isOnline
                    .drop(1)
                    .collect { online ->
                        if (online) {
                            hideError()
                            viewModel.refresh()
                        } else {
                            showError(getString(R.string.error_no_internet))
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
        recyclerView.visibility = View.GONE
        adView.visibility = View.GONE
    }

    private fun setData(
        state: UiState.Success
    ) = with(binding) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        adView.visibility = View.VISIBLE
        categoryAdapter.submitList(state.categories)
        hideError()
    }

    private fun setError(
        errorMessage: String
    ) = with(binding) {
        progressBar.visibility = View.GONE
        recyclerView.visibility =
            if (categoryAdapter.itemCount > 0) View.VISIBLE
            else View.GONE

        adView.visibility =
            if (categoryAdapter.itemCount > 0) View.VISIBLE
            else View.GONE

        showError(errorMessage)
    }

    private fun showError(message: String) {
        snackbar?.dismiss()
        snackbar = Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.retry) {
            viewModel.refresh()
        }.setActionTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.textSecondary
            )
        )
        snackbar?.show()
    }

    private fun hideError() {
        snackbar?.dismiss()
        snackbar = null
    }
}
