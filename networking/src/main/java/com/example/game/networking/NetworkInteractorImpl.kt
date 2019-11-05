package com.example.game.networking

import android.util.Log
import com.example.game.controllers.*
import com.example.game.domain.game.Mark
import com.example.game.networking.i9e.*
import com.google.flatbuffers.FlatBufferBuilder
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.CoroutineContext


class NetworkInteractorImpl : NetworkInteractor, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private val channel = ManagedChannelBuilder.forAddress(srvAddress, srvPort).usePlaintext().build()
    private val asyncStub: GameConfiguratorGrpc.GameConfiguratorStub = GameConfiguratorGrpc.newStub(channel)
    private var gameWrapper: NetworkClient? = null
    private val fbb = FlatBufferBuilder(1024)

    companion object {
        const val NI_TAG = "NetworkInteractorImpl"
        const val srvAddress = "192.168.1.107"
        const val srvPort = 8080
    }

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.Creator(inbox: ReceiveChannel<CreatorRequest>): ReceiveChannel<CrResponse> = produce(capacity = 3) {
        val cancelException = io.grpc.StatusException(io.grpc.Status.CANCELLED.withDescription("cancel the game"))
        var thrownByClient = false

        val creatorRespObserver = object : StreamObserver<CrResponse> {
            override fun onNext(value: CrResponse?) {
                Log.d(NI_TAG, "response received")
                this@Creator.launch {
                    send(value!!)
                }
            }

            override fun onError(t: Throwable) {
                if (thrownByClient) {
                    close()
                } else {
                    close(t)
                }
            }

            override fun onCompleted() {
                close()
            }
        }
        val creatorReqObserver: StreamObserver<CrRequest> = try {
            asyncStub.createGame(creatorRespObserver)
        } catch (e: IOException) {
            close(e)
            return@produce
        }

        for (msg in inbox) {
            when (msg) {
                is CrReq -> {
                    creatorReqObserver.onNext(msg.request)
                }
                is CrDisconnect -> {
                    thrownByClient = true
                    creatorReqObserver.onError(cancelException)
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.Joiner(inbox: ReceiveChannel<OpponentRequest>) = produce(capacity = 3) {
        val joinRespObserver = object : StreamObserver<OppResponse> {
            override fun onNext(value: OppResponse?) {
                this@Joiner.launch {
                    send(value!!)
                }
            }

            override fun onError(t: Throwable?) {
                close(t!!)
            }

            override fun onCompleted() {
                close()
            }
        }
        val joinReqObserver: StreamObserver<OppRequest> = try {
            asyncStub.joinGame(joinRespObserver)
        } catch (e: IOException) {
            close(e)
            return@produce
        }

        for (msg in inbox) {
            when (msg) {
                is OppReq -> joinReqObserver.onNext(msg.request)
                is OppDisconnect -> joinReqObserver.onError(io.grpc.StatusException(
                        io.grpc.Status.CANCELLED.withDescription("cancel the game")))
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun CreateGame(): GameInitializer {
        val reqChan = Channel<CreatorRequest>(Channel.UNLIMITED)
        val respChan = Creator(reqChan)
        val wrapper = GameCreatorWrapper(reqChan, respChan)

        gameWrapper = wrapper
        return wrapper
    }

    @ExperimentalCoroutinesApi
    private fun listFlow(filter: GameFilter): Flow<GameItem> = callbackFlow {
        val GLObserver = object : StreamObserver<ListItem> {
            override fun onNext(value: ListItem) {
                val params = value.params()!!
                offer(GameItem(value.ID(), params.rows().toInt(), params.cols().toInt(), params.win().toInt(), Mark.values()[params.mark().toInt()]))
            }

            override fun onError(t: Throwable) {
                println("WTF: $t")
                close(t)
            }

            override fun onCompleted() {
                close()
            }
        }
        asyncStub.getListOfGames(filter, GLObserver)

        awaitClose()
    }

    @ExperimentalCoroutinesApi
    override fun GameList(rowRange: ParamRange, colRange: ParamRange, winRange: ParamRange, markF: Byte): Flow<GameItem> {
        val rowF = Range.createRange(fbb, rowRange.start, rowRange.end)
        val colF = Range.createRange(fbb, colRange.start, colRange.end)
        val winF = Range.createRange(fbb, winRange.start, winRange.end)
        fbb.finish(GameFilter.createGameFilter(fbb, rowF, colF, winF, markF))
        val filter = GameFilter.getRootAsGameFilter(fbb.dataBuffer())
        return listFlow(filter)
    }

    @ExperimentalCoroutinesApi
    override fun JoinGame(gameID: Short): ReceiveChannel<GameCreationStatus> {
        val reqChan = Channel<OpponentRequest>(Channel.UNLIMITED)
        val respChan = Joiner(reqChan)

        val wrapper = GameOpponentWrapper(reqChan, respChan)
        gameWrapper = wrapper
        return wrapper.joinGame(gameID)
    }

    override fun getGameClientWrapper(): NetworkClient {
        if (gameWrapper == null) {
            throw IllegalStateException("game is not initialized")
        }
        return gameWrapper!!
    }
}

sealed class CreatorRequest
data class CrReq(val request: CrRequest) : CreatorRequest()
object CrDisconnect : CreatorRequest()

sealed class OpponentRequest
data class OppReq(val request: OppRequest) : OpponentRequest()
object OppDisconnect : OpponentRequest()