// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: network.proto

package com.example.transport;

/**
 * Protobuf type {@code transport.CreateResponse}
 */
public  final class CreateResponse extends
    com.google.protobuf.GeneratedMessageLite<
        CreateResponse, CreateResponse.Builder> implements
    // @@protoc_insertion_point(message_implements:transport.CreateResponse)
    CreateResponseOrBuilder {
  private CreateResponse() {
  }
  private int bitField0_;
  private int payloadCase_ = 0;
  private java.lang.Object payload_;
  public enum PayloadCase {
    STATUS(1),
    WIN_LINE(2),
    GAME_ID(4),
    PAYLOAD_NOT_SET(0);
    private final int value;
    private PayloadCase(int value) {
      this.value = value;
    }
    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static PayloadCase valueOf(int value) {
      return forNumber(value);
    }

    public static PayloadCase forNumber(int value) {
      switch (value) {
        case 1: return STATUS;
        case 2: return WIN_LINE;
        case 4: return GAME_ID;
        case 0: return PAYLOAD_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  @java.lang.Override
  public PayloadCase
  getPayloadCase() {
    return PayloadCase.forNumber(
        payloadCase_);
  }

  private void clearPayload() {
    payloadCase_ = 0;
    payload_ = null;
  }

  public static final int STATUS_FIELD_NUMBER = 1;
  /**
   * <code>.base.GameStatus status = 1;</code>
   * @return Whether the status field is set.
   */
  @java.lang.Override
  public boolean hasStatus() {
    return payloadCase_ == 1;
  }
  /**
   * <code>.base.GameStatus status = 1;</code>
   * @return The enum numeric value on the wire for status.
   */
  @java.lang.Override
  public int getStatusValue() {
    if (payloadCase_ == 1) {
      return (java.lang.Integer) payload_;
    }
    return 0;
  }
  /**
   * <code>.base.GameStatus status = 1;</code>
   * @return The status.
   */
  @java.lang.Override
  public com.example.transport.GameStatus getStatus() {
    if (payloadCase_ == 1) {
      com.example.transport.GameStatus result = com.example.transport.GameStatus.forNumber((java.lang.Integer) payload_);
      return result == null ? com.example.transport.GameStatus.UNRECOGNIZED : result;
    }
    return com.example.transport.GameStatus.GAME_STATUS_UNSPECIFIED;
  }
  /**
   * <code>.base.GameStatus status = 1;</code>
   * @param value The enum numeric value on the wire for status to set.
   */
  private void setStatusValue(int value) {
    payloadCase_ = 1;
    payload_ = value;
  }
  /**
   * <code>.base.GameStatus status = 1;</code>
   * @param value The status to set.
   */
  private void setStatus(com.example.transport.GameStatus value) {
    payload_ = value.getNumber();
    payloadCase_ = 1;
  }
  /**
   * <code>.base.GameStatus status = 1;</code>
   */
  private void clearStatus() {
    if (payloadCase_ == 1) {
      payloadCase_ = 0;
      payload_ = null;
    }
  }

  public static final int WIN_LINE_FIELD_NUMBER = 2;
  /**
   * <code>.base.WinLine win_line = 2;</code>
   */
  @java.lang.Override
  public boolean hasWinLine() {
    return payloadCase_ == 2;
  }
  /**
   * <code>.base.WinLine win_line = 2;</code>
   */
  @java.lang.Override
  public com.example.transport.WinLine getWinLine() {
    if (payloadCase_ == 2) {
       return (com.example.transport.WinLine) payload_;
    }
    return com.example.transport.WinLine.getDefaultInstance();
  }
  /**
   * <code>.base.WinLine win_line = 2;</code>
   */
  private void setWinLine(com.example.transport.WinLine value) {
    value.getClass();
  payload_ = value;
    payloadCase_ = 2;
  }
  /**
   * <code>.base.WinLine win_line = 2;</code>
   */
  private void mergeWinLine(com.example.transport.WinLine value) {
    value.getClass();
  if (payloadCase_ == 2 &&
        payload_ != com.example.transport.WinLine.getDefaultInstance()) {
      payload_ = com.example.transport.WinLine.newBuilder((com.example.transport.WinLine) payload_)
          .mergeFrom(value).buildPartial();
    } else {
      payload_ = value;
    }
    payloadCase_ = 2;
  }
  /**
   * <code>.base.WinLine win_line = 2;</code>
   */
  private void clearWinLine() {
    if (payloadCase_ == 2) {
      payloadCase_ = 0;
      payload_ = null;
    }
  }

  public static final int GAME_ID_FIELD_NUMBER = 4;
  /**
   * <code>uint32 game_id = 4;</code>
   * @return Whether the gameId field is set.
   */
  @java.lang.Override
  public boolean hasGameId() {
    return payloadCase_ == 4;
  }
  /**
   * <code>uint32 game_id = 4;</code>
   * @return The gameId.
   */
  @java.lang.Override
  public int getGameId() {
    if (payloadCase_ == 4) {
      return (java.lang.Integer) payload_;
    }
    return 0;
  }
  /**
   * <code>uint32 game_id = 4;</code>
   * @param value The gameId to set.
   */
  private void setGameId(int value) {
    payloadCase_ = 4;
    payload_ = value;
  }
  /**
   * <code>uint32 game_id = 4;</code>
   */
  private void clearGameId() {
    if (payloadCase_ == 4) {
      payloadCase_ = 0;
      payload_ = null;
    }
  }

  public static final int MOVE_FIELD_NUMBER = 5;
  private com.example.transport.Move move_;
  /**
   * <code>optional .base.Move move = 5;</code>
   */
  @java.lang.Override
  public boolean hasMove() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional .base.Move move = 5;</code>
   */
  @java.lang.Override
  public com.example.transport.Move getMove() {
    return move_ == null ? com.example.transport.Move.getDefaultInstance() : move_;
  }
  /**
   * <code>optional .base.Move move = 5;</code>
   */
  private void setMove(com.example.transport.Move value) {
    value.getClass();
  move_ = value;
    bitField0_ |= 0x00000001;
    }
  /**
   * <code>optional .base.Move move = 5;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeMove(com.example.transport.Move value) {
    value.getClass();
  if (move_ != null &&
        move_ != com.example.transport.Move.getDefaultInstance()) {
      move_ =
        com.example.transport.Move.newBuilder(move_).mergeFrom(value).buildPartial();
    } else {
      move_ = value;
    }
    bitField0_ |= 0x00000001;
  }
  /**
   * <code>optional .base.Move move = 5;</code>
   */
  private void clearMove() {  move_ = null;
    bitField0_ = (bitField0_ & ~0x00000001);
  }

  public static com.example.transport.CreateResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.example.transport.CreateResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.example.transport.CreateResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.example.transport.CreateResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.example.transport.CreateResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.example.transport.CreateResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.example.transport.CreateResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.example.transport.CreateResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.example.transport.CreateResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.example.transport.CreateResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.example.transport.CreateResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.example.transport.CreateResponse parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.example.transport.CreateResponse prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code transport.CreateResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.example.transport.CreateResponse, Builder> implements
      // @@protoc_insertion_point(builder_implements:transport.CreateResponse)
      com.example.transport.CreateResponseOrBuilder {
    // Construct using com.example.transport.CreateResponse.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }

    @java.lang.Override
    public PayloadCase
        getPayloadCase() {
      return instance.getPayloadCase();
    }

    public Builder clearPayload() {
      copyOnWrite();
      instance.clearPayload();
      return this;
    }


    /**
     * <code>.base.GameStatus status = 1;</code>
     * @return Whether the status field is set.
     */
    @java.lang.Override
    public boolean hasStatus() {
      return instance.hasStatus();
    }
    /**
     * <code>.base.GameStatus status = 1;</code>
     * @return The enum numeric value on the wire for status.
     */
    @java.lang.Override
    public int getStatusValue() {
      return instance.getStatusValue();
    }
    /**
     * <code>.base.GameStatus status = 1;</code>
     * @param value The enum numeric value on the wire for status to set.
     * @return This builder for chaining.
     */
    public Builder setStatusValue(int value) {
      copyOnWrite();
      instance.setStatusValue(value);
      return this;
    }
    /**
     * <code>.base.GameStatus status = 1;</code>
     * @return The status.
     */
    @java.lang.Override
    public com.example.transport.GameStatus getStatus() {
      return instance.getStatus();
    }
    /**
     * <code>.base.GameStatus status = 1;</code>
     * @param value The status to set.
     * @return This builder for chaining.
     */
    public Builder setStatus(com.example.transport.GameStatus value) {
      copyOnWrite();
      instance.setStatus(value);
      return this;
    }
    /**
     * <code>.base.GameStatus status = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearStatus() {
      copyOnWrite();
      instance.clearStatus();
      return this;
    }

    /**
     * <code>.base.WinLine win_line = 2;</code>
     */
    @java.lang.Override
    public boolean hasWinLine() {
      return instance.hasWinLine();
    }
    /**
     * <code>.base.WinLine win_line = 2;</code>
     */
    @java.lang.Override
    public com.example.transport.WinLine getWinLine() {
      return instance.getWinLine();
    }
    /**
     * <code>.base.WinLine win_line = 2;</code>
     */
    public Builder setWinLine(com.example.transport.WinLine value) {
      copyOnWrite();
      instance.setWinLine(value);
      return this;
    }
    /**
     * <code>.base.WinLine win_line = 2;</code>
     */
    public Builder setWinLine(
        com.example.transport.WinLine.Builder builderForValue) {
      copyOnWrite();
      instance.setWinLine(builderForValue.build());
      return this;
    }
    /**
     * <code>.base.WinLine win_line = 2;</code>
     */
    public Builder mergeWinLine(com.example.transport.WinLine value) {
      copyOnWrite();
      instance.mergeWinLine(value);
      return this;
    }
    /**
     * <code>.base.WinLine win_line = 2;</code>
     */
    public Builder clearWinLine() {
      copyOnWrite();
      instance.clearWinLine();
      return this;
    }

    /**
     * <code>uint32 game_id = 4;</code>
     * @return Whether the gameId field is set.
     */
    @java.lang.Override
    public boolean hasGameId() {
      return instance.hasGameId();
    }
    /**
     * <code>uint32 game_id = 4;</code>
     * @return The gameId.
     */
    @java.lang.Override
    public int getGameId() {
      return instance.getGameId();
    }
    /**
     * <code>uint32 game_id = 4;</code>
     * @param value The gameId to set.
     * @return This builder for chaining.
     */
    public Builder setGameId(int value) {
      copyOnWrite();
      instance.setGameId(value);
      return this;
    }
    /**
     * <code>uint32 game_id = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearGameId() {
      copyOnWrite();
      instance.clearGameId();
      return this;
    }

    /**
     * <code>optional .base.Move move = 5;</code>
     */
    @java.lang.Override
    public boolean hasMove() {
      return instance.hasMove();
    }
    /**
     * <code>optional .base.Move move = 5;</code>
     */
    @java.lang.Override
    public com.example.transport.Move getMove() {
      return instance.getMove();
    }
    /**
     * <code>optional .base.Move move = 5;</code>
     */
    public Builder setMove(com.example.transport.Move value) {
      copyOnWrite();
      instance.setMove(value);
      return this;
      }
    /**
     * <code>optional .base.Move move = 5;</code>
     */
    public Builder setMove(
        com.example.transport.Move.Builder builderForValue) {
      copyOnWrite();
      instance.setMove(builderForValue.build());
      return this;
    }
    /**
     * <code>optional .base.Move move = 5;</code>
     */
    public Builder mergeMove(com.example.transport.Move value) {
      copyOnWrite();
      instance.mergeMove(value);
      return this;
    }
    /**
     * <code>optional .base.Move move = 5;</code>
     */
    public Builder clearMove() {  copyOnWrite();
      instance.clearMove();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:transport.CreateResponse)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.example.transport.CreateResponse();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "payload_",
            "payloadCase_",
            "bitField0_",
            com.example.transport.WinLine.class,
            "move_",
          };
          java.lang.String info =
              "\u0000\u0004\u0001\u0001\u0001\u0005\u0004\u0000\u0000\u0000\u0001?\u0000\u0002<" +
              "\u0000\u0004>\u0000\u0005\u1009\u0000";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.example.transport.CreateResponse> parser = PARSER;
        if (parser == null) {
          synchronized (com.example.transport.CreateResponse.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.example.transport.CreateResponse>(
                      DEFAULT_INSTANCE);
              PARSER = parser;
            }
          }
        }
        return parser;
    }
    case GET_MEMOIZED_IS_INITIALIZED: {
      return (byte) 1;
    }
    case SET_MEMOIZED_IS_INITIALIZED: {
      return null;
    }
    }
    throw new UnsupportedOperationException();
  }


  // @@protoc_insertion_point(class_scope:transport.CreateResponse)
  private static final com.example.transport.CreateResponse DEFAULT_INSTANCE;
  static {
    CreateResponse defaultInstance = new CreateResponse();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      CreateResponse.class, defaultInstance);
  }

  public static com.example.transport.CreateResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<CreateResponse> PARSER;

  public static com.google.protobuf.Parser<CreateResponse> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
