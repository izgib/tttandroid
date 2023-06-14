package com.example.game.tic_tac_toe.gamesetup

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.controllers.models.GameType
import com.example.controllers.models.ParamRange
import com.example.game.GameRules
import com.example.game.tic_tac_toe.R
import com.example.game.tic_tac_toe.navigation.base.backstack
import com.example.game.tic_tac_toe.navigation.base.bluetooth
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.base.network
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.navigation.scopes.GameCreator
import com.example.game.tic_tac_toe.navigation.scopes.toSettings
import com.example.game.tic_tac_toe.ui_components.ButtonProgressComponent
import com.example.game.tic_tac_toe.ui_components.ColsCount
import com.example.game.tic_tac_toe.ui_components.DeterminateProgress
import com.example.game.tic_tac_toe.ui_components.GameSetupPlayersComponent
import com.example.game.tic_tac_toe.ui_components.GameSetupPlayersState
import com.example.game.tic_tac_toe.ui_components.GameSetupSizeComponent
import com.example.game.tic_tac_toe.ui_components.GameSetupSizeState
import com.example.game.tic_tac_toe.ui_components.IndeterminateProgress
import com.example.game.tic_tac_toe.ui_components.PlayerO
import com.example.game.tic_tac_toe.ui_components.PlayerX
import com.example.game.tic_tac_toe.ui_components.Reshuffle
import com.example.game.tic_tac_toe.ui_components.RowsCount
import com.example.game.tic_tac_toe.ui_components.WinCount
import com.example.transport.Application
import com.example.transport.BluetoothLEInteractor
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class GameSetup : Fragment() {
    private val config by lazy<GameConfig> { lookup() }
    private val initializer by lazy<GameCreator> { lookup() }
    private lateinit var makeDiscoverable: ActivityResultLauncher<Intent>
    private val isDiscoverable = MutableStateFlow<Boolean?>(null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        //if (resources.getBoolean(R.bool.gameSetupFull)) {
        makeDiscoverable =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                isDiscoverable.value = when (result.resultCode) {
                    AppCompatActivity.RESULT_CANCELED -> false
                    else -> true
                }
            }

        val view = inflater.inflate(R.layout.game_setup_full, container, false)

        val gameSize = GameSetupSizeComponent.bind(
            view,
            GameSetupSizeState(
                ParamRange(GameRules.ROWS_MIN, GameRules.ROWS_MAX),
                config.rows,
                ParamRange(GameRules.COLS_MIN, GameRules.COLS_MAX),
                config.cols,
                ParamRange(GameRules.WIN_MIN, GameRules.WIN_MAX),
                config.win
            ),
        )
        val playersSetup = GameSetupPlayersComponent.bind(
            view, GameSetupPlayersState(
                config.gameType, config.player1, config.player2
            )
        )


        val button = view.findViewById<Button>(R.id.game_create)
        if (config.gameType == GameType.Local) {
            button.apply {
                text = "Создать игру"
                setOnClickListener { _ ->
                    createGame(config)
                }
            }
        } else run {
            val state = when (config.gameType) {
                GameType.Network -> IndeterminateProgress(
                    false, 100, "Отменить игру", "Создать игру",
                )

                GameType.BluetoothClassic -> DeterminateProgress(
                    false, 100, 60000, "Отключить игоу", "Создать игру",
                )

                GameType.BluetoothLE -> IndeterminateProgress(
                    false, 100, "Отменить игру", "Создать игру"
                )

                GameType.Local -> return@run
            }
            val rootLayout = view as ConstraintLayout

            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            val progress = ProgressBar(
                ContextThemeWrapper(
                    inflater.context, R.style.Widget_AppCompat_ProgressBar_Horizontal
                ), null, 0
            ).apply {
                id = ViewCompat.generateViewId()
                layoutParams = params
                rootLayout.addView(this, params)
            }

            val gameIdLabel: TextView? = if (config.gameType == GameType.Network) {
                val params = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
                TextView(inflater.context).apply {
                    id = ViewCompat.generateViewId()
                    layoutParams = params
                    text = "lol"
                    rootLayout.addView(this, params)
                }
            } else null

            val constraintSet = ConstraintSet().apply { clone(rootLayout) }
            constraintSet.connect(
                progress.id, ConstraintSet.TOP, button.id, ConstraintSet.BOTTOM
            )
            gameIdLabel?.let { label ->
                constraintSet.connect(
                    label.id, ConstraintSet.TOP, progress.id, ConstraintSet.BOTTOM
                )
                constraintSet.connect(
                    label.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START
                )
                constraintSet.connect(
                    label.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END
                )
            }
            constraintSet.applyTo(rootLayout)

            val gameCreation = ButtonProgressComponent(button, progress, state)


            val connector = when (config.gameType) {
                GameType.Network -> initializer.network
                GameType.BluetoothLE -> initializer.bluetoothLE
                GameType.BluetoothClassic -> initializer.bluetoothClassic
                GameType.Local -> throw IllegalStateException()
            }

            viewLifecycleOwner.lifecycleScope.launch {
                gameCreation.getUserInteractionEvents().collect { created ->
                    if (created) {
                        connector.create(config.toSettings())
                    } else {
                        println("try to cancel")
                        connector.cancel()
                    }
                }
            }
            viewLifecycleOwner.lifecycleScope.launch {
                connector.started.collect { started ->
                    gameCreation.enabled = started
                }
            }

            gameIdLabel?.let { label ->
                viewLifecycleOwner.lifecycleScope.launch {
                    initializer.network.gameID.filterNotNull().collect { id ->
                        label.text = "Game ID: $id"
                    }
                }
            }

        }

        with(viewLifecycleOwner.lifecycleScope) {
            launch {
                gameSize.getUserInteractionEvents().collect { settings ->
                    when (settings) {
                        is RowsCount -> {
                            config.rows = settings.value
                            gameSize.updateWin(kotlin.math.min(config.rows, config.cols))
                        }

                        is ColsCount -> {
                            config.cols = settings.value
                            gameSize.updateWin(kotlin.math.min(config.rows, config.cols))
                        }

                        is WinCount -> config.win = settings.value
                    }
                }
            }
            launch {
                playersSetup.getUserInteractionEvents().collect { settings ->
                    when (settings) {
                        is PlayerX -> config.player1 = settings.player
                        is PlayerO -> config.player2 = settings.player
                        Reshuffle -> {
                            config.reshuffle()
                            playersSetup.setPlayerX(config.player1)
                            playersSetup.setPlayerO(config.player2)
                        }
                    }
                }
            }
        }
        return view

        /*        return StepperViewBinding.inflate(inflater, container, false).root.apply {
                    adapter = GameSetupStepperAdapter(
                            requireContext(),
                            viewLifecycleOwner.lifecycleScope,
                            config,
                            this@GameSetup::createGame,
                    )
                }*/
    }

    private fun createGame(config: GameConfig) {

        if (!config.gameConfigured()) {
            Snackbar.make(requireView(), "Настройте игру", Snackbar.LENGTH_SHORT).show()
            return
        }

        when (config.gameType) {
            GameType.Local -> initializer.createLocalGame(config)
            GameType.BluetoothClassic -> startBluetoothGame()
            GameType.Network -> startNetworkGame()
            GameType.BluetoothLE -> startBluetoothLEGame()
        }
    }

    private fun startBluetoothGame() {
        if (!backstack.bluetooth.bluetoothEnabled()) {
            Snackbar.make(requireView(), "Телефон нельзя обнаружить", Snackbar.LENGTH_LONG).show()
            return
        }

        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            //putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, timeSec * 1000)
        }
        makeDiscoverable.launch(discoverableIntent)
        viewLifecycleOwner.lifecycleScope.launch {
            val result = isDiscoverable.filterNotNull().first()
            isDiscoverable.value = null
            if (!result) {
                return@launch
            }

            /*initializer.createBluetoothGame(config).onEach { state ->
                when (state) {
                    is Awaiting -> Log.d("LOL BT", "типа анимация загрузки")
                    is Connected -> {
                    }

                    is CreatingFailure -> Log.e("LOL BT", "got error")
                }
            }.launchIn(initializer.scope)*/
            initializer.bluetoothClassic.create(config.toSettings())
        }
    }


    private fun startNetworkGame() {
        if (!backstack.network.haveInternet()) {
            Snackbar.make(requireView(), "Включите Интернет", Snackbar.LENGTH_LONG).show()
            return
        }

        initializer.network.create(config.toSettings())
    }

    private fun startBluetoothLEGame() {
        initializer.bluetoothLE.create(config.toSettings())
    }
}