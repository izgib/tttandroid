// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: network.proto

package com.example.transport;

/**
 * Protobuf type {@code transport.Range}
 */
public  final class Range extends
    com.google.protobuf.GeneratedMessageLite<
        Range, Range.Builder> implements
    // @@protoc_insertion_point(message_implements:transport.Range)
    RangeOrBuilder {
  private Range() {
  }
  public static final int START_FIELD_NUMBER = 1;
  private int start_;
  /**
   * <code>uint32 start = 1;</code>
   * @return The start.
   */
  @java.lang.Override
  public int getStart() {
    return start_;
  }
  /**
   * <code>uint32 start = 1;</code>
   * @param value The start to set.
   */
  private void setStart(int value) {
    
    start_ = value;
  }
  /**
   * <code>uint32 start = 1;</code>
   */
  private void clearStart() {
    
    start_ = 0;
  }

  public static final int END_FIELD_NUMBER = 2;
  private int end_;
  /**
   * <code>uint32 end = 2;</code>
   * @return The end.
   */
  @java.lang.Override
  public int getEnd() {
    return end_;
  }
  /**
   * <code>uint32 end = 2;</code>
   * @param value The end to set.
   */
  private void setEnd(int value) {
    
    end_ = value;
  }
  /**
   * <code>uint32 end = 2;</code>
   */
  private void clearEnd() {
    
    end_ = 0;
  }

  public static com.example.transport.Range parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.example.transport.Range parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.example.transport.Range parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.example.transport.Range parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.example.transport.Range parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.example.transport.Range parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.example.transport.Range parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.example.transport.Range parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.example.transport.Range parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.example.transport.Range parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.example.transport.Range parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.example.transport.Range parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.example.transport.Range prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code transport.Range}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.example.transport.Range, Builder> implements
      // @@protoc_insertion_point(builder_implements:transport.Range)
      com.example.transport.RangeOrBuilder {
    // Construct using com.example.transport.Range.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>uint32 start = 1;</code>
     * @return The start.
     */
    @java.lang.Override
    public int getStart() {
      return instance.getStart();
    }
    /**
     * <code>uint32 start = 1;</code>
     * @param value The start to set.
     * @return This builder for chaining.
     */
    public Builder setStart(int value) {
      copyOnWrite();
      instance.setStart(value);
      return this;
    }
    /**
     * <code>uint32 start = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearStart() {
      copyOnWrite();
      instance.clearStart();
      return this;
    }

    /**
     * <code>uint32 end = 2;</code>
     * @return The end.
     */
    @java.lang.Override
    public int getEnd() {
      return instance.getEnd();
    }
    /**
     * <code>uint32 end = 2;</code>
     * @param value The end to set.
     * @return This builder for chaining.
     */
    public Builder setEnd(int value) {
      copyOnWrite();
      instance.setEnd(value);
      return this;
    }
    /**
     * <code>uint32 end = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearEnd() {
      copyOnWrite();
      instance.clearEnd();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:transport.Range)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.example.transport.Range();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "start_",
            "end_",
          };
          java.lang.String info =
              "\u0000\u0002\u0000\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001\u000b\u0002\u000b" +
              "";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.example.transport.Range> parser = PARSER;
        if (parser == null) {
          synchronized (com.example.transport.Range.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.example.transport.Range>(
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


  // @@protoc_insertion_point(class_scope:transport.Range)
  private static final com.example.transport.Range DEFAULT_INSTANCE;
  static {
    Range defaultInstance = new Range();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      Range.class, defaultInstance);
  }

  public static com.example.transport.Range getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<Range> PARSER;

  public static com.google.protobuf.Parser<Range> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
