package com.example.transport.service

import com.example.controllers.*
import com.example.controllers.models.Range
import com.example.game.Mark
import com.example.transport.*
import com.example.transport.ClientAction
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.IOException


class NetworkInteractor private constructor(channel: io.grpc.Channel) {
    //private var channel: io.grpc.Channel = ManagedChannelBuilder.forAddress(srvAddress, srvPort).usePlaintext().build()
    private val asyncStub: GameConfiguratorGrpc.GameConfiguratorStub =
        GameConfiguratorGrpc.newStub(channel)
    private var gameWrapper: NetworkClient? = null

    companion object {
        const val NI_TAG = "NetworkInteractorImpl"
        const val srvAddress = "192.168.3.2"
        const val srvPort = 8080

        internal fun testInstance(channel: io.grpc.Channel) = NetworkInteractor(channel)
        fun newInstance() = NetworkInteractor(
            ManagedChannelBuilder.forAddress(srvAddress, srvPort).usePlaintext().build()
        )
    }

    private fun CoroutineScope.creator(inbox: ReceiveChannel<CreatorRequest>): ReceiveChannel<CreateResponse> =
        produce(capacity = 3) {
            invokeOnClose { println("response channel closed: creator") }
            val cancelException =
                io.grpc.StatusException(io.grpc.Status.CANCELLED.withDescription("cancel the game"))
            var thrownByClient = false

            val creatorRespObserver = object : StreamObserver<CreateResponse> {
                override fun onNext(value: CreateResponse) {
                    trySend(value)
                }

                override fun onError(t: Throwable) {
                    inbox.cancel(CancellationException(t.message, t))
                    close(t)
                    /*if (thrownByClient) {
                        close()
                    } else {
                        println("closed with exception")
                        close(t)
                    }*/
                }

                override fun onCompleted() {
                    close()
                }
            }
            val creatorReqObserver: StreamObserver<CreateRequest> = try {
                asyncStub.createGame(creatorRespObserver)
            } catch (e: IOException) {
                close(e)
                return@produce
            }

            for (msg in inbox) {
                var finish = false
                creatorReqObserver.onNext(when (msg) {
                    is CrReq -> msg.request
                    is CrAction -> createRequest {
                        action = when (msg.action) {
                            PlayerAction.Leave -> {
                                finish = true
                                ClientAction.CLIENT_ACTION_LEAVE
                            }
                            PlayerAction.GiveUp -> ClientAction.CLIENT_ACTION_GIVE_UP
                        }
                    }
                })
                if (finish) {
                    creatorReqObserver.onCompleted()
                    return@produce
                }
            }
        }

    private fun CoroutineScope.joiner(inbox: ReceiveChannel<OpponentRequest>) =
        produce<JoinResponse>(capacity = 3) {
            val joinRespObserver = object : StreamObserver<JoinResponse> {
                override fun onNext(value: JoinResponse) {
                    trySend(value)
                }

                override fun onError(t: Throwable) {
                    inbox.cancel(CancellationException(t.message, t))
                    close(t)
                }

                override fun onCompleted() {
                    close()
                }
            }
            val joinReqObserver: StreamObserver<JoinRequest> = try {
                asyncStub.joinGame(joinRespObserver)
            } catch (e: IOException) {
                close(e)
                return@produce
            }

            for (msg in inbox) {
                println("request received")
                var finish = false
                joinReqObserver.onNext(when (msg) {
                    is OppReq -> msg.request
                    is OppAction -> joinRequest {
                        action = when (msg.action) {
                            PlayerAction.Leave -> {
                                finish = true
                                ClientAction.CLIENT_ACTION_LEAVE
                            }
                            PlayerAction.GiveUp -> ClientAction.CLIENT_ACTION_GIVE_UP
                        }
                    }
                })
                if (finish) {
                    joinReqObserver.onCompleted()
                    return@produce
                }
            }
        }


    fun CreateGame(scope: CoroutineScope): GameInitializer {
        val reqChan = Channel<CreatorRequest>()
        val respChan = scope.creator(reqChan)
        val wrapper = GameCreatorWrapper(reqChan, respChan, scope)

        gameWrapper = wrapper
        return wrapper
    }

    private fun listFlow(filter: GameFilter): Flow<GameItem> = callbackFlow {
        val GLObserver = object : StreamObserver<ListItem> {
            override fun onNext(value: ListItem) {
                val settings: GameSettings = value.params.run {
                    GameSettings(rows, cols, win, mark.toMark())
                }
                trySend(GameItem(value.id, settings))
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


    fun GameList(rowRange: Range, colRange: Range, winRange: Range, mark: Mark): Flow<GameItem> {
        return listFlow(gameFilter {
            rows = range {
                start = rowRange.start
                end = rowRange.end
            }

            cols = range {
                start = colRange.start
                end = colRange.end
            }
            win = range {
                start = winRange.start
                end = winRange.end
            }
            this.mark = mark.toMarkType()
        })
    }


    fun JoinGame(scope: CoroutineScope, gameID: Int): Flow<GameCreationStatus> {
        val reqChan = Channel<OpponentRequest>()
        val respChan = scope.joiner(reqChan)

        val wrapper = GameOpponentWrapper(reqChan, respChan, scope)
        gameWrapper = wrapper
        return wrapper.joinGame(gameID)
    }

    fun getGameClientWrapper(): NetworkClient {
        if (gameWrapper == null) {
            throw IllegalStateException("game is not initialized")
        }
        return gameWrapper!!
    }
}

sealed class CreatorRequest
class CrReq(val request: CreateRequest) : CreatorRequest()
class CrAction(val action: PlayerAction) : CreatorRequest()

sealed class OpponentRequest
class OppReq(val request: JoinRequest) : OpponentRequest()
class OppAction(val action: PlayerAction) : OpponentRequest()