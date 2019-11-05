package com.example.game.tic_tac_toe.gamesetup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.game.controllers.*
import com.example.game.domain.game.Mark
import com.example.game.tic_tac_toe.GameApplication
import com.example.game.tic_tac_toe.NotificationReceiver
import com.example.game.tic_tac_toe.R
import com.example.game.tic_tac_toe.databinding.GameSetupStepPlayersBinding
import com.example.game.tic_tac_toe.viewmodels.GameInitializerModel
import com.example.game.tic_tac_toe.viewmodels.GameSetupViewModel
import com.example.game.tic_tac_toe.viewmodels.SensorsViewModel
import com.google.android.material.snackbar.Snackbar
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.whileSelect
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class GameSetupPlayers : Fragment(), Step, GameCreator {
    private lateinit var gameCreate: Button


    private lateinit var snackbar: Snackbar
    private val sensors: SensorsViewModel by sharedViewModel()

    private val GSViewModel: GameSetupViewModel by sharedViewModel()
    private val GIModel: GameInitializerModel by sharedViewModel()

    companion object {
        const val TAG = "GameSetupPlayers"
        private const val CHANNEL_ID = "TIC_TAC_TOE"
        private val notID = 1337
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = GameSetupStepPlayersBinding.inflate(inflater, container, false)
        val players = getOpponents(context!!, GSViewModel.getGameType())
        binding.spinAdapter = OpponentChoose(context!!, R.layout.opponent_spinner, players)
        binding.gameSetup = GSViewModel
        binding.handler = this
        gameCreate = binding.gameCreate
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
    }


    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun createGame(gameType: GameType) {
        if (!GSViewModel.gameConfigured()) {
            Snackbar.make(gameCreate, "Настройте игру", Snackbar.LENGTH_SHORT).show()
            return
        }

        when (gameType) {
            GameType.Local -> startLocalGame()
            GameType.Bluetooth -> startBluetoothGame()
            GameType.Network -> startNetworkGame()
        }
    }

    private fun startLocalGame() {
        findNavController().navigate(R.id.action_gameSetup_to_gameFragment)
    }

    @ExperimentalCoroutinesApi
    private fun startBluetoothGame() {
        if (!sensors.bluetoothEnabled()) {
            val discoverableListener = sensors.makeBluetoothDiscoverable()
            discoverableListener.observe(this, object : Observer<Boolean> {
                override fun onChanged(t: Boolean?) {
                    Snackbar.make(gameCreate, "Телефон нельзя обнаружить", Snackbar.LENGTH_LONG).show()
                    discoverableListener.removeObserver(this)
                }
            })
            return
        }
        if (GSViewModel.player1 != PlayerType.Bluetooth) {
            GSViewModel.mark = Mark.Cross
        }
        if (GSViewModel.player2 != PlayerType.Bluetooth) {
            GSViewModel.mark = Mark.Nought
        }
        GIModel.createBt(GSViewModel.rows.value!!, GSViewModel.cols.value!!, GSViewModel.win.value!!,
                GSViewModel.mark).observe(this, Observer { state ->
            when (state) {
                is GameInitStatus.Awaiting -> Log.d("LOL BT", "типа анимация загрузки")
                is GameInitStatus.Connected -> findNavController().navigate(R.id.action_gameSetup_to_gameFragment)
                is GameInitStatus.Failure -> Log.e("LOL BT", "got error")
            }
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = "tic_tac_toe"
            val descriptionText = "game awaiting"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun notTemplate(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context!!, CHANNEL_ID)
                .setSmallIcon(R.mipmap.game_launcher)
                .setLargeIcon(ResourcesCompat.getDrawable(
                        resources, R.mipmap.game_launcher, null)!!.toBitmap())
                .setContentTitle("Tic-Tac-Toe")
                .setPriority(NotificationCompat.PRIORITY_MAX)
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private fun startNetworkGame() {
        if (!sensors.networkEnabled()) {
            Snackbar.make(gameCreate, "Включите Интернет", Snackbar.LENGTH_LONG).show()
            return
        }
        if (GSViewModel.player1 != PlayerType.Network) {
            GSViewModel.mark = Mark.Cross
        }
        if (GSViewModel.player2 != PlayerType.Network) {
            GSViewModel.mark = Mark.Nought
        }

        val pendingReturnIntent = PendingIntent.getActivity(
                context!!.applicationContext, 0,
                Intent(context, GameApplication::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_ONE_SHOT
        )

        val cancelPendingIntent = PendingIntent.getBroadcast(
                context!!.applicationContext,
                1,
                Intent(NotificationReceiver.CANCEL_GAME),
                PendingIntent.FLAG_ONE_SHOT
        )

        val initializer = GIModel.CreateNt()

        val notReceiver = NotificationReceiver()
        notReceiver.registerCallback {
            initializer.CancelGame()
            NotificationManagerCompat.from(context!!).cancel(notID)
            context!!.applicationContext.unregisterReceiver(notReceiver)
        }
        val filter = IntentFilter(NotificationReceiver.CANCEL_GAME)
        context!!.applicationContext.registerReceiver(notReceiver, filter)

        GIModel.viewModelScope.launch {
            val chan = initializer.sendCreationRequest(GSViewModel.rows.value!!, GSViewModel.cols.value!!, GSViewModel.win.value!!, GSViewModel.mark)

            val state = withTimeoutOrNull(100L) {
                return@withTimeoutOrNull chan.receive()
            }

            val builder = notTemplate()
                    .setProgress(0, 0, true)
                    .addAction(android.R.drawable.ic_delete, "Cancel", cancelPendingIntent)

            if (state != null) {
                when (state) {
                    is CreationFailure -> {
                        snackbar = Snackbar.make(view!!, "Can not connect to server", Snackbar.LENGTH_LONG)
                        snackbar.show()
                        return@launch
                    }
                    is GameID -> builder.setContentText("Game ID: ${state.ID}")
                }
            }

            with(NotificationManagerCompat.from(context!!)) {
                notify(notID, builder.build())
            }

            whileSelect {
                chan.onReceiveOrClosed { value ->
                    if (value.isClosed) {
                        return@onReceiveOrClosed false
                    }
                    when (val state = value.value) {
                        is GameID -> {
                            val notification = builder
                                    .setContentText("Game ID: ${state.ID}")
                                    .build()
                            with(NotificationManagerCompat.from(context!!)) {
                                notify(notID, notification)
                            }
                            true
                        }
                        is Created -> {
                            with(NotificationManagerCompat.from(context!!)) {
                                notify(notID,
                                        notTemplate()
                                                .setContentText("Opponent joined the game")
                                                .setContentIntent(pendingReturnIntent)
                                                .setAutoCancel(true)
                                                .build()
                                )
                            }
                            findNavController().navigate(R.id.action_gameSetup_to_gameFragment)
                            false
                        }
                        is CreationFailure -> {
                            with(NotificationManagerCompat.from(context!!)) {
                                notify(1337, notTemplate().setContentText("Creation Fail").build())
                            }
                            false
                        }
                        else -> throw IllegalStateException()
                    }
                }
            }
        }
    }

    override fun onSelected() {

    }

    override fun verifyStep(): VerificationError {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(error: VerificationError) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

interface GameCreator {
    fun createGame(gameType: GameType)
}

data class Opponent(val img: Drawable, val oName: String)

class OpponentChoose(context: Context, resource: Int, private val opps: ArrayList<Opponent>) : ArrayAdapter<Opponent>(context, resource, opps) {
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getDropDownView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val opp = inflater.inflate(R.layout.opponent_spinner, parent, false)
        val name = opp.findViewById(R.id.opponent_name) as TextView
        name.setCompoundDrawablesWithIntrinsicBounds(opps[position].img, null, null, null)
        name.text = opps[position].oName
        return opp
    }

}

// 1. Bot, 2.Human, 3.(Nobody|Bluetooth|Network)
fun getOpponents(context: Context, gameType: GameType): ArrayList<Opponent> {
    val size = when (gameType) {
        GameType.Local -> 2
        GameType.Bluetooth, GameType.Network -> 3
    }
    val data = ArrayList<Opponent>(size)

    var img = ContextCompat.getDrawable(context, R.drawable.ic_android_green_24dp)!!
    var name = "bot"
    data.add(Opponent(img, name))

    img = ContextCompat.getDrawable(context, R.drawable.ic_person_blue_24dp)!!
    name = "human"
    data.add(Opponent(img, name))

    if (gameType == GameType.Local)
        return data

    when (gameType) {
        GameType.Bluetooth -> {
            img = ContextCompat.getDrawable(context, R.drawable.ic_bluetooth_blue_24dp)!!
            name = "bluetooth"
        }
        GameType.Network -> {
            img = ContextCompat.getDrawable(context, R.drawable.ic_public_blue_24dp)!!
            name = "network"
        }
    }
    data.add(Opponent(img, name))

    return data
}