// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: bluetooth.proto

package com.example.transport;

/**
 * Protobuf type {@code ClientMessage}
 */
public  final class ClientMessage extends
    com.google.protobuf.GeneratedMessageLite<
        ClientMessage, ClientMessage.Builder> implements
    // @@protoc_insertion_point(message_implements:ClientMessage)
    ClientMessageOrBuilder {
  private ClientMessage() {
  }
  private int payloadCase_ = 0;
  private java.lang.Object payload_;
  public enum PayloadCase {
    MOVE(1),
    ACTION(2),
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
        case 1: return MOVE;
        case 2: return ACTION;
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

  public static final int MOVE_FIELD_NUMBER = 1;
  /**
   * <code>.base.Move move = 1;</code>
   */
  @java.lang.Override
  public boolean hasMove() {
    return payloadCase_ == 1;
  }
  /**
   * <code>.base.Move move = 1;</code>
   */
  @java.lang.Override
  public com.example.transport.Move getMove() {
    if (payloadCase_ == 1) {
       return (com.example.transport.Move) payload_;
    }
    return com.example.transport.Move.getDefaultInstance();
  }
  /**
   * <code>.base.Move move = 1;</code>
   */
  private void setMove(com.example.transport.Move value) {
    value.getClass();
  payload_ = value;
    payloadCase_ = 1;
  }
  /**
   * <code>.base.Move move = 1;</code>
   */
  private void mergeMove(com.example.transport.Move value) {
    value.getClass();
  if (payloadCase_ == 1 &&
        payload_ != com.example.transport.Move.getDefaultInstance()) {
      payload_ = com.example.transport.Move.newBuilder((com.example.transport.Move) payload_)
          .mergeFrom(value).buildPartial();
    } else {
      payload_ = value;
    }
    payloadCase_ = 1;
  }
  /**
   * <code>.base.Move move = 1;</code>
   */
  private void clearMove() {
    if (payloadCase_ == 1) {
      payloadCase_ = 0;
      payload_ = null;
    }
  }

  public static final int ACTION_FIELD_NUMBER = 2;
  /**
   * <code>.base.ClientAction action = 2;</code>
   * @return Whether the action field is set.
   */
  @java.lang.Override
  public boolean hasAction() {
    return payloadCase_ == 2;
  }
  /**
   * <code>.base.ClientAction action = 2;</code>
   * @return The enum numeric value on the wire for action.
   */
  @java.lang.Override
  public int getActionValue() {
    if (payloadCase_ == 2) {
      return (java.lang.Integer) payload_;
    }
    return 0;
  }
  /**
   * <code>.base.ClientAction action = 2;</code>
   * @return The action.
   */
  @java.lang.Override
  public com.example.transport.ClientAction getAction() {
    if (payloadCase_ == 2) {
      com.example.transport.ClientAction result = com.example.transport.ClientAction.forNumber((java.lang.Integer) payload_);
      return result == null ? com.example.transport.ClientAction.UNRECOGNIZED : result;
    }
    return com.example.transport.ClientAction.CLIENT_ACTION_LEAVE;
  }
  /**
   * <code>.base.ClientAction action = 2;</code>
   * @param value The enum numeric value on the wire for action to set.
   */
  private void setActionValue(int value) {
    payloadCase_ = 2;
    payload_ = value;
  }
  /**
   * <code>.base.ClientAction action = 2;</code>
   * @param value The action to set.
   */
  private void setAction(com.example.transport.ClientAction value) {
    payload_ = value.getNumber();
    payloadCase_ = 2;
  }
  /**
   * <code>.base.ClientAction action = 2;</code>
   */
  private void clearAction() {
    if (payloadCase_ == 2) {
      payloadCase_ = 0;
      payload_ = null;
    }
  }

  public static com.example.transport.ClientMessage parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.example.transport.ClientMessage parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.example.transport.ClientMessage parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.example.transport.ClientMessage parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.example.transport.ClientMessage parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.example.transport.ClientMessage parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.example.transport.ClientMessage parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.example.transport.ClientMessage parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.example.transport.ClientMessage parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.example.transport.ClientMessage parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.example.transport.ClientMessage parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.example.transport.ClientMessage parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.example.transport.ClientMessage prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code ClientMessage}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.example.transport.ClientMessage, Builder> implements
      // @@protoc_insertion_point(builder_implements:ClientMessage)
      com.example.transport.ClientMessageOrBuilder {
    // Construct using com.example.transport.ClientMessage.newBuilder()
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
     * <code>.base.Move move = 1;</code>
     */
    @java.lang.Override
    public boolean hasMove() {
      return instance.hasMove();
    }
    /**
     * <code>.base.Move move = 1;</code>
     */
    @java.lang.Override
    public com.example.transport.Move getMove() {
      return instance.getMove();
    }
    /**
     * <code>.base.Move move = 1;</code>
     */
    public Builder setMove(com.example.transport.Move value) {
      copyOnWrite();
      instance.setMove(value);
      return this;
    }
    /**
     * <code>.base.Move move = 1;</code>
     */
    public Builder setMove(
        com.example.transport.Move.Builder builderForValue) {
      copyOnWrite();
      instance.setMove(builderForValue.build());
      return this;
    }
    /**
     * <code>.base.Move move = 1;</code>
     */
    public Builder mergeMove(com.example.transport.Move value) {
      copyOnWrite();
      instance.mergeMove(value);
      return this;
    }
    /**
     * <code>.base.Move move = 1;</code>
     */
    public Builder clearMove() {
      copyOnWrite();
      instance.clearMove();
      return this;
    }

    /**
     * <code>.base.ClientAction action = 2;</code>
     * @return Whether the action field is set.
     */
    @java.lang.Override
    public boolean hasAction() {
      return instance.hasAction();
    }
    /**
     * <code>.base.ClientAction action = 2;</code>
     * @return The enum numeric value on the wire for action.
     */
    @java.lang.Override
    public int getActionValue() {
      return instance.getActionValue();
    }
    /**
     * <code>.base.ClientAction action = 2;</code>
     * @param value The enum numeric value on the wire for action to set.
     * @return This builder for chaining.
     */
    public Builder setActionValue(int value) {
      copyOnWrite();
      instance.setActionValue(value);
      return this;
    }
    /**
     * <code>.base.ClientAction action = 2;</code>
     * @return The action.
     */
    @java.lang.Override
    public com.example.transport.ClientAction getAction() {
      return instance.getAction();
    }
    /**
     * <code>.base.ClientAction action = 2;</code>
     * @param value The action to set.
     * @return This builder for chaining.
     */
    public Builder setAction(com.example.transport.ClientAction value) {
      copyOnWrite();
      instance.setAction(value);
      return this;
    }
    /**
     * <code>.base.ClientAction action = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearAction() {
      copyOnWrite();
      instance.clearAction();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:ClientMessage)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.example.transport.ClientMessage();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "payload_",
            "payloadCase_",
            com.example.transport.Move.class,
          };
          java.lang.String info =
              "\u0000\u0002\u0001\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001<\u0000\u0002?" +
              "\u0000";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.example.transport.ClientMessage> parser = PARSER;
        if (parser == null) {
          synchronized (com.example.transport.ClientMessage.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.example.transport.ClientMessage>(
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


  // @@protoc_insertion_point(class_scope:ClientMessage)
  private static final com.example.transport.ClientMessage DEFAULT_INSTANCE;
  static {
    ClientMessage defaultInstance = new ClientMessage();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      ClientMessage.class, defaultInstance);
  }

  public static com.example.transport.ClientMessage getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<ClientMessage> PARSER;

  public static com.google.protobuf.Parser<ClientMessage> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

