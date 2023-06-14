package com.example.game.tic_tac_toe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.coroutineScope
import com.example.game.tic_tac_toe.databinding.RootLinearLayoutBinding
import com.example.game.tic_tac_toe.navigation.base.BaseFragment
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.scopes.TypeStorage
import com.example.game.tic_tac_toe.ui_components.GameTypeComponent
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainFragment : BaseFragment() {
    private val gameType: TypeStorage by lazy { lookup() }
    private lateinit var container: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = RootLinearLayoutBinding.inflate(layoutInflater, container, false)
        this.container = binding.root
        return this.container
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycle.coroutineScope.launch {
            GameTypeComponent(container).getUserInteractionEvents().collect { type ->
                gameType.gameType = type
            }
        }
    }
}
