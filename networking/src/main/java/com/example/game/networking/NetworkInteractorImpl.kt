package com.example.game.networking

import com.example.game.controllers.*
import com.example.game.controllers.models.Range
import com.example.game.domain.game.Mark
import com.example.game.networking.i9e.*
import com.google.flatbuffers.FlatBufferBuilder
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.IOException
import com.example.game.networking.i9e.Range as FBRange


class NetworkInteractorImpl private constructor(channel: io.grpc.Channel) : NetworkInteractor {
    //private var channel: io.grpc.Channel = ManagedChannelBuilder.forAddress(srvAddress, srvPort).usePlaintext().build()
    private val asyncStub: GameConfiguratorGrpc.GameConfiguratorStub = GameConfiguratorGrpc.newStub(channel)
    private var gameWrapper: NetworkClient? = null
    private val fbb = FlatBufferBuilder(1024)

    companion object {
        const val NI_TAG = "NetworkInteractorImpl"
        const val srvAddress = "192.168.1.107"
        const val srvPort = 8080

        internal fun testInstance(channel: io.grpc.Channel) = NetworkInteractorImpl(channel)
        fun newInstance() = NetworkInteractorImpl(ManagedChannelBuilder.forAddress(srvAddress, srvPort).usePlaintext().build())
    }

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.creator(inbox: ReceiveChannel<CreatorRequest>): ReceiveChannel<CrResponse> = produce(capacity = 3) {
        invokeOnClose { println("response channel closed: creator") }
        val cancelException = io.grpc.StatusException(io.grpc.Status.CANCELLED.withDescription("cancel the game"))
        var thrownByClient = false

        val creatorRespObserver = object : StreamObserver<CrResponse> {
            override fun onNext(value: CrResponse) {
                println("got response in creator function")
                sendBlocking(value)
                println("successfully send response to chan")
            }

            override fun onError(t: Throwable) {
                println("got error: $t")
                println("request channel canceled")
                inbox.cancel(CancellationException(t.message, t))
                //inbox.cancel()
                if (thrownByClient) {
                    close()
                } else {
                    println("closed with exception")
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
    private fun CoroutineScope.joiner(inbox: ReceiveChannel<OpponentRequest>) = produce<OppResponse>(capacity = 3) {
        invokeOnClose { println("response channel closed: joiner") }
        val cancelException = io.grpc.StatusException(io.grpc.Status.CANCELLED.withDescription("cancel the game"))
        var thrownByClient = false
        val joinRespObserver = object : StreamObserver<OppResponse> {
            override fun onNext(value: OppResponse) {
                sendBlocking(value)
            }

            override fun onError(t: Throwable) {
                println("got error")
                println("request channel canceled")
                inbox.cancel(CancellationException(t.message, t))
                if (thrownByClient) {
                    close()
                } else {
                    println("send error token")
                    close(t)
                }
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
            println("request received")
            when (msg) {
                is OppReq -> joinReqObserver.onNext(msg.request)
                is OppDisconnect -> {
                    thrownByClient = true
                    joinReqObserver.onError(cancelException)
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun CreateGame(scope: CoroutineScope): GameInitializer {
        val reqChan = Channel<CreatorRequest>()
        val respChan = scope.creator(reqChan)
        val wrapper = GameCreatorWrapper(reqChan, respChan, scope)

        gameWrapper = wrapper
        return wrapper
    }

    @ExperimentalCoroutinesApi
    private fun listFlow(filter: GameFilter): Flow<GameItem> = callbackFlow {
        val GLObserver = object : StreamObserver<ListItem> {
            override fun onNext(value: ListItem) {
                val settings: GameSettings = value.params()!!.run {
                    GameSettings(rows().toInt(), cols().toInt(), win().toInt(), Mark.values()[mark().toInt()])
                }
                sendBlocking(GameItem(value.ID(), settings))
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
    override fun GameList(rowRange: Range, colRange: Range, winRange: Range, mark: Mark): Flow<GameItem> {
        val rowF = FBRange.createRange(fbb, rowRange.start.toShort(), rowRange.end.toShort())
        val colF = FBRange.createRange(fbb, colRange.start.toShort(), colRange.end.toShort())
        val winF = FBRange.createRange(fbb, winRange.start.toShort(), winRange.end.toShort())
        val fbMark = when (mark) {
            Mark.Cross -> MarkTypeFilter.Cross
            Mark.Nought -> MarkTypeFilter.Nought
            Mark.Empty -> MarkTypeFilter.Any
        }
        fbb.finish(GameFilter.createGameFilter(fbb, rowF, colF, winF, fbMark))
        val filter = GameFilter.getRootAsGameFilter(fbb.dataBuffer())
        return listFlow(filter)
    }

    @ExperimentalCoroutinesApi
    override fun JoinGame(scope: CoroutineScope, gameID: Short): Flow<GameCreationStatus> {
        val reqChan = Channel<OpponentRequest>()
        val respChan = scope.joiner(reqChan)

        val wrapper = GameOpponentWrapper(reqChan, respChan, scope)
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