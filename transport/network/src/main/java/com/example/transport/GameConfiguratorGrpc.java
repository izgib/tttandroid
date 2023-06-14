package com.example.transport;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.51.1)",
    comments = "Source: network.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class GameConfiguratorGrpc {

  private GameConfiguratorGrpc() {}

  public static final String SERVICE_NAME = "transport.GameConfigurator";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.example.transport.GameFilter,
      com.example.transport.ListItem> getGetListOfGamesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetListOfGames",
      requestType = com.example.transport.GameFilter.class,
      responseType = com.example.transport.ListItem.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.example.transport.GameFilter,
      com.example.transport.ListItem> getGetListOfGamesMethod() {
    io.grpc.MethodDescriptor<com.example.transport.GameFilter, com.example.transport.ListItem> getGetListOfGamesMethod;
    if ((getGetListOfGamesMethod = GameConfiguratorGrpc.getGetListOfGamesMethod) == null) {
      synchronized (GameConfiguratorGrpc.class) {
        if ((getGetListOfGamesMethod = GameConfiguratorGrpc.getGetListOfGamesMethod) == null) {
          GameConfiguratorGrpc.getGetListOfGamesMethod = getGetListOfGamesMethod =
              io.grpc.MethodDescriptor.<com.example.transport.GameFilter, com.example.transport.ListItem>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetListOfGames"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  com.example.transport.GameFilter.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  com.example.transport.ListItem.getDefaultInstance()))
              .build();
        }
      }
    }
    return getGetListOfGamesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.transport.CreateRequest,
      com.example.transport.CreateResponse> getCreateGameMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateGame",
      requestType = com.example.transport.CreateRequest.class,
      responseType = com.example.transport.CreateResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<com.example.transport.CreateRequest,
      com.example.transport.CreateResponse> getCreateGameMethod() {
    io.grpc.MethodDescriptor<com.example.transport.CreateRequest, com.example.transport.CreateResponse> getCreateGameMethod;
    if ((getCreateGameMethod = GameConfiguratorGrpc.getCreateGameMethod) == null) {
      synchronized (GameConfiguratorGrpc.class) {
        if ((getCreateGameMethod = GameConfiguratorGrpc.getCreateGameMethod) == null) {
          GameConfiguratorGrpc.getCreateGameMethod = getCreateGameMethod =
              io.grpc.MethodDescriptor.<com.example.transport.CreateRequest, com.example.transport.CreateResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateGame"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  com.example.transport.CreateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  com.example.transport.CreateResponse.getDefaultInstance()))
              .build();
        }
      }
    }
    return getCreateGameMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.example.transport.JoinRequest,
      com.example.transport.JoinResponse> getJoinGameMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "JoinGame",
      requestType = com.example.transport.JoinRequest.class,
      responseType = com.example.transport.JoinResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<com.example.transport.JoinRequest,
      com.example.transport.JoinResponse> getJoinGameMethod() {
    io.grpc.MethodDescriptor<com.example.transport.JoinRequest, com.example.transport.JoinResponse> getJoinGameMethod;
    if ((getJoinGameMethod = GameConfiguratorGrpc.getJoinGameMethod) == null) {
      synchronized (GameConfiguratorGrpc.class) {
        if ((getJoinGameMethod = GameConfiguratorGrpc.getJoinGameMethod) == null) {
          GameConfiguratorGrpc.getJoinGameMethod = getJoinGameMethod =
              io.grpc.MethodDescriptor.<com.example.transport.JoinRequest, com.example.transport.JoinResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "JoinGame"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  com.example.transport.JoinRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  com.example.transport.JoinResponse.getDefaultInstance()))
              .build();
        }
      }
    }
    return getJoinGameMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static GameConfiguratorStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GameConfiguratorStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GameConfiguratorStub>() {
        @java.lang.Override
        public GameConfiguratorStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GameConfiguratorStub(channel, callOptions);
        }
      };
    return GameConfiguratorStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static GameConfiguratorBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GameConfiguratorBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GameConfiguratorBlockingStub>() {
        @java.lang.Override
        public GameConfiguratorBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GameConfiguratorBlockingStub(channel, callOptions);
        }
      };
    return GameConfiguratorBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static GameConfiguratorFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GameConfiguratorFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GameConfiguratorFutureStub>() {
        @java.lang.Override
        public GameConfiguratorFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GameConfiguratorFutureStub(channel, callOptions);
        }
      };
    return GameConfiguratorFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class GameConfiguratorImplBase implements io.grpc.BindableService {

    /**
     */
    public void getListOfGames(com.example.transport.GameFilter request,
        io.grpc.stub.StreamObserver<com.example.transport.ListItem> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetListOfGamesMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.example.transport.CreateRequest> createGame(
        io.grpc.stub.StreamObserver<com.example.transport.CreateResponse> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getCreateGameMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.example.transport.JoinRequest> joinGame(
        io.grpc.stub.StreamObserver<com.example.transport.JoinResponse> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getJoinGameMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetListOfGamesMethod(),
            io.grpc.stub.ServerCalls.asyncServerStreamingCall(
              new MethodHandlers<
                com.example.transport.GameFilter,
                com.example.transport.ListItem>(
                  this, METHODID_GET_LIST_OF_GAMES)))
          .addMethod(
            getCreateGameMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                com.example.transport.CreateRequest,
                com.example.transport.CreateResponse>(
                  this, METHODID_CREATE_GAME)))
          .addMethod(
            getJoinGameMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                com.example.transport.JoinRequest,
                com.example.transport.JoinResponse>(
                  this, METHODID_JOIN_GAME)))
          .build();
    }
  }

  /**
   */
  public static final class GameConfiguratorStub extends io.grpc.stub.AbstractAsyncStub<GameConfiguratorStub> {
    private GameConfiguratorStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GameConfiguratorStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GameConfiguratorStub(channel, callOptions);
    }

    /**
     */
    public void getListOfGames(com.example.transport.GameFilter request,
        io.grpc.stub.StreamObserver<com.example.transport.ListItem> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getGetListOfGamesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.example.transport.CreateRequest> createGame(
        io.grpc.stub.StreamObserver<com.example.transport.CreateResponse> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getCreateGameMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.example.transport.JoinRequest> joinGame(
        io.grpc.stub.StreamObserver<com.example.transport.JoinResponse> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getJoinGameMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class GameConfiguratorBlockingStub extends io.grpc.stub.AbstractBlockingStub<GameConfiguratorBlockingStub> {
    private GameConfiguratorBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GameConfiguratorBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GameConfiguratorBlockingStub(channel, callOptions);
    }

    /**
     */
    public java.util.Iterator<com.example.transport.ListItem> getListOfGames(
        com.example.transport.GameFilter request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getGetListOfGamesMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class GameConfiguratorFutureStub extends io.grpc.stub.AbstractFutureStub<GameConfiguratorFutureStub> {
    private GameConfiguratorFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GameConfiguratorFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GameConfiguratorFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_GET_LIST_OF_GAMES = 0;
  private static final int METHODID_CREATE_GAME = 1;
  private static final int METHODID_JOIN_GAME = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final GameConfiguratorImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(GameConfiguratorImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_LIST_OF_GAMES:
          serviceImpl.getListOfGames((com.example.transport.GameFilter) request,
              (io.grpc.stub.StreamObserver<com.example.transport.ListItem>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CREATE_GAME:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.createGame(
              (io.grpc.stub.StreamObserver<com.example.transport.CreateResponse>) responseObserver);
        case METHODID_JOIN_GAME:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.joinGame(
              (io.grpc.stub.StreamObserver<com.example.transport.JoinResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (GameConfiguratorGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .addMethod(getGetListOfGamesMethod())
              .addMethod(getCreateGameMethod())
              .addMethod(getJoinGameMethod())
              .build();
        }
      }
    }
    return result;
  }
}
