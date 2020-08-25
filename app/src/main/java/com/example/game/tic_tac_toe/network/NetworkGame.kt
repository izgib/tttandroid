package com.example.game.tic_tac_toe.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.coroutineScope
import com.example.game.tic_tac_toe.databinding.RootLinearLayoutBinding
import com.example.game.tic_tac_toe.navigation.base.BaseFragment
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.scopes.CreatorStorage
import com.example.game.tic_tac_toe.ui_components.Create
import com.example.game.tic_tac_toe.ui_components.Find
import com.example.game.tic_tac_toe.ui_components.NetworkGameComponent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class NetworkGame : BaseFragment() {
    private val creator by lazy<CreatorStorage> { lookup() }
    private lateinit var container: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = RootLinearLayoutBinding.inflate(layoutInflater, container, false)
        this.container = binding.root
        return this.container
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycle.coroutineScope.launch {
            NetworkGameComponent(container).getUserInteractionEvents().collect { action ->
                when (action) {
                    is Create -> creator.isCreator = true
                    is Find -> creator.isCreator = false
                }
            }
        }
    }

    companion object {
        const val TAG = "InetGame"
    }
}

