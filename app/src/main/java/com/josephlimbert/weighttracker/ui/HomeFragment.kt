package com.josephlimbert.weighttracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.josephlimbert.weighttracker.data.model.Weight
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: Fragment() {
    @Inject lateinit var weightViewModel: WeightViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val weightList: List<Weight> = ArrayList<Weight>()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                weightViewModel.weightList.collect { list ->
                    weightList = list
                }
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}