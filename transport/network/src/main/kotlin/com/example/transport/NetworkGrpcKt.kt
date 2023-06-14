package com.example.transport

import com.example.transport.GameConfiguratorGrpc.getServiceDescriptor
import io.grpc.CallOptions
import io.grpc.CallOptions.DEFAULT
import io.grpc.Channel
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServerServiceDefinition
import io.grpc.ServerServiceDefinition.builder
import io.grpc.ServiceDescriptor
import io.grpc.Status
import io.grpc.Status.UNIMPLEMENTED
import io.grpc.StatusException
import io.grpc.kotlin.AbstractCoroutineServerImpl
import io.grpc.kotlin.AbstractCoroutineStub
import io.grpc.kotlin.ClientCalls
import io.grpc.kotlin.ClientCalls.bidiStreamingRpc
import io.grpc.kotlin.ClientCalls.serverStreamingRpc
import io.grpc.kotlin.ServerCalls
import io.grpc.kotlin.ServerCalls.bidiStreamingServerMethodDefinition
import io.grpc.kotlin.ServerCalls.serverStreamingServerMethodDefinition
import io.grpc.kotlin.StubFor
import kotlin.String
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlinx.coroutines.flow.Flow

/**
 * Holder for Kotlin coroutine-based client and server APIs for transport.GameConfigurator.
 */
public object GameConfiguratorGrpcKt {
  public const val SERVICE_NAME: String = GameConfiguratorGrpc.SERVICE_NAME

  @JvmStatic
  public val serviceDescriptor: ServiceDescriptor
    get() = GameConfiguratorGrpc.getServiceDescriptor()

  public val getListOfGamesMethod: MethodDescriptor<GameFilter, ListItem>
    @JvmStatic
    get() = GameConfiguratorGrpc.getGetListOfGamesMethod()

  public val createGameMethod: MethodDescriptor<CreateRequest, CreateResponse>
    @JvmStatic
    get() = GameConfiguratorGrpc.getCreateGameMethod()

  public val joinGameMethod: MethodDescriptor<JoinRequest, JoinResponse>
    @JvmStatic
    get() = GameConfiguratorGrpc.getJoinGameMethod()

  /**
   * A stub for issuing RPCs to a(n) transport.GameConfigurator service as suspending coroutines.
   */
  @StubFor(GameConfiguratorGrpc::class)
  public class GameConfiguratorCoroutineStub @JvmOverloads constructor(
    channel: Channel,
    callOptions: CallOptions = DEFAULT,
  ) : AbstractCoroutineStub<GameConfiguratorCoroutineStub>(channel, callOptions) {
    public override fun build(channel: Channel, callOptions: CallOptions):
        GameConfiguratorCoroutineStub = GameConfiguratorCoroutineStub(channel, callOptions)

    /**
     * Returns a [Flow] that, when collected, executes this RPC and emits responses from the
     * server as they arrive.  That flow finishes normally if the server closes its response with
     * [`Status.OK`][Status], and fails by throwing a [StatusException] otherwise.  If
     * collecting the flow downstream fails exceptionally (including via cancellation), the RPC
     * is cancelled with that exception as a cause.
     *
     * @param request The request message to send to the server.
     *
     * @param headers Metadata to attach to the request.  Most users will not need this.
     *
     * @return A flow that, when collected, emits the responses from the server.
     */
    public fun getListOfGames(request: GameFilter, headers: Metadata = Metadata()): Flow<ListItem> =
        serverStreamingRpc(
      channel,
      GameConfiguratorGrpc.getGetListOfGamesMethod(),
      request,
      callOptions,
      headers
    )

    /**
     * Returns a [Flow] that, when collected, executes this RPC and emits responses from the
     * server as they arrive.  That flow finishes normally if the server closes its response with
     * [`Status.OK`][Status], and fails by throwing a [StatusException] otherwise.  If
     * collecting the flow downstream fails exceptionally (including via cancellation), the RPC
     * is cancelled with that exception as a cause.
     *
     * The [Flow] of requests is collected once each time the [Flow] of responses is
     * collected. If collection of the [Flow] of responses completes normally or
     * exceptionally before collection of `requests` completes, the collection of
     * `requests` is cancelled.  If the collection of `requests` completes
     * exceptionally for any other reason, then the collection of the [Flow] of responses
     * completes exceptionally for the same reason and the RPC is cancelled with that reason.
     *
     * @param requests A [Flow] of request messages.
     *
     * @param headers Metadata to attach to the request.  Most users will not need this.
     *
     * @return A flow that, when collected, emits the responses from the server.
     */
    public fun createGame(requests: Flow<CreateRequest>, headers: Metadata = Metadata()):
        Flow<CreateResponse> = bidiStreamingRpc(
      channel,
      GameConfiguratorGrpc.getCreateGameMethod(),
      requests,
      callOptions,
      headers
    )

    /**
     * Returns a [Flow] that, when collected, executes this RPC and emits responses from the
     * server as they arrive.  That flow finishes normally if the server closes its response with
     * [`Status.OK`][Status], and fails by throwing a [StatusException] otherwise.  If
     * collecting the flow downstream fails exceptionally (including via cancellation), the RPC
     * is cancelled with that exception as a cause.
     *
     * The [Flow] of requests is collected once each time the [Flow] of responses is
     * collected. If collection of the [Flow] of responses completes normally or
     * exceptionally before collection of `requests` completes, the collection of
     * `requests` is cancelled.  If the collection of `requests` completes
     * exceptionally for any other reason, then the collection of the [Flow] of responses
     * completes exceptionally for the same reason and the RPC is cancelled with that reason.
     *
     * @param requests A [Flow] of request messages.
     *
     * @param headers Metadata to attach to the request.  Most users will not need this.
     *
     * @return A flow that, when collected, emits the responses from the server.
     */
    public fun joinGame(requests: Flow<JoinRequest>, headers: Metadata = Metadata()):
        Flow<JoinResponse> = bidiStreamingRpc(
      channel,
      GameConfiguratorGrpc.getJoinGameMethod(),
      requests,
      callOptions,
      headers
    )
  }

  /**
   * Skeletal implementation of the transport.GameConfigurator service based on Kotlin coroutines.
   */
  public abstract class GameConfiguratorCoroutineImplBase(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
  ) : AbstractCoroutineServerImpl(coroutineContext) {
    /**
     * Returns a [Flow] of responses to an RPC for transport.GameConfigurator.GetListOfGames.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status
     * `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param request The request from the client.
     */
    public open fun getListOfGames(request: GameFilter): Flow<ListItem> = throw
        StatusException(UNIMPLEMENTED.withDescription("Method transport.GameConfigurator.GetListOfGames is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for transport.GameConfigurator.CreateGame.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status
     * `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param requests A [Flow] of requests from the client.  This flow can be
     *        collected only once and throws [java.lang.IllegalStateException] on attempts to
     * collect
     *        it more than once.
     */
    public open fun createGame(requests: Flow<CreateRequest>): Flow<CreateResponse> = throw
        StatusException(UNIMPLEMENTED.withDescription("Method transport.GameConfigurator.CreateGame is unimplemented"))

    /**
     * Returns a [Flow] of responses to an RPC for transport.GameConfigurator.JoinGame.
     *
     * If creating or collecting the returned flow fails with a [StatusException], the RPC
     * will fail with the corresponding [Status].  If it fails with a
     * [java.util.concurrent.CancellationException], the RPC will fail with status
     * `Status.CANCELLED`.  If creating
     * or collecting the returned flow fails for any other reason, the RPC will fail with
     * `Status.UNKNOWN` with the exception as a cause.
     *
     * @param requests A [Flow] of requests from the client.  This flow can be
     *        collected only once and throws [java.lang.IllegalStateException] on attempts to
     * collect
     *        it more than once.
     */
    public open fun joinGame(requests: Flow<JoinRequest>): Flow<JoinResponse> = throw
        StatusException(UNIMPLEMENTED.withDescription("Method transport.GameConfigurator.JoinGame is unimplemented"))

    public final override fun bindService(): ServerServiceDefinition =
        builder(getServiceDescriptor())
      .addMethod(serverStreamingServerMethodDefinition(
      context = this.context,
      descriptor = GameConfiguratorGrpc.getGetListOfGamesMethod(),
      implementation = ::getListOfGames
    ))
      .addMethod(bidiStreamingServerMethodDefinition(
      context = this.context,
      descriptor = GameConfiguratorGrpc.getCreateGameMethod(),
      implementation = ::createGame
    ))
      .addMethod(bidiStreamingServerMethodDefinition(
      context = this.context,
      descriptor = GameConfiguratorGrpc.getJoinGameMethod(),
      implementation = ::joinGame
    )).build()
  }
}
