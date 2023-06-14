// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: base.proto

package com.example.transport;

/**
 * Protobuf type {@code base.GameParams}
 */
public  final class GameParams extends
    com.google.protobuf.GeneratedMessageLite<
        GameParams, GameParams.Builder> implements
    // @@protoc_insertion_point(message_implements:base.GameParams)
    GameParamsOrBuilder {
  private GameParams() {
  }
  public static final int ROWS_FIELD_NUMBER = 1;
  private int rows_;
  /**
   * <code>uint32 rows = 1;</code>
   * @return The rows.
   */
  @java.lang.Override
  public int getRows() {
    return rows_;
  }
  /**
   * <code>uint32 rows = 1;</code>
   * @param value The rows to set.
   */
  private void setRows(int value) {
    
    rows_ = value;
  }
  /**
   * <code>uint32 rows = 1;</code>
   */
  private void clearRows() {
    
    rows_ = 0;
  }

  public static final int COLS_FIELD_NUMBER = 2;
  private int cols_;
  /**
   * <code>uint32 cols = 2;</code>
   * @return The cols.
   */
  @java.lang.Override
  public int getCols() {
    return cols_;
  }
  /**
   * <code>uint32 cols = 2;</code>
   * @param value The cols to set.
   */
  private void setCols(int value) {
    
    cols_ = value;
  }
  /**
   * <code>uint32 cols = 2;</code>
   */
  private void clearCols() {
    
    cols_ = 0;
  }

  public static final int WIN_FIELD_NUMBER = 3;
  private int win_;
  /**
   * <code>uint32 win = 3;</code>
   * @return The win.
   */
  @java.lang.Override
  public int getWin() {
    return win_;
  }
  /**
   * <code>uint32 win = 3;</code>
   * @param value The win to set.
   */
  private void setWin(int value) {
    
    win_ = value;
  }
  /**
   * <code>uint32 win = 3;</code>
   */
  private void clearWin() {
    
    win_ = 0;
  }

  public static final int MARK_FIELD_NUMBER = 4;
  private int mark_;
  /**
   * <code>.base.MarkType mark = 4;</code>
   * @return The enum numeric value on the wire for mark.
   */
  @java.lang.Override
  public int getMarkValue() {
    return mark_;
  }
  /**
   * <code>.base.MarkType mark = 4;</code>
   * @return The mark.
   */
  @java.lang.Override
  public com.example.transport.MarkType getMark() {
    com.example.transport.MarkType result = com.example.transport.MarkType.forNumber(mark_);
    return result == null ? com.example.transport.MarkType.UNRECOGNIZED : result;
  }
  /**
   * <code>.base.MarkType mark = 4;</code>
   * @param value The enum numeric value on the wire for mark to set.
   */
  private void setMarkValue(int value) {
      mark_ = value;
  }
  /**
   * <code>.base.MarkType mark = 4;</code>
   * @param value The mark to set.
   */
  private void setMark(com.example.transport.MarkType value) {
    mark_ = value.getNumber();
    
  }
  /**
   * <code>.base.MarkType mark = 4;</code>
   */
  private void clearMark() {
    
    mark_ = 0;
  }

  public static com.example.transport.GameParams parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.example.transport.GameParams parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.example.transport.GameParams parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.example.transport.GameParams parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.example.transport.GameParams parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static com.example.transport.GameParams parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static com.example.transport.GameParams parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.example.transport.GameParams parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.example.transport.GameParams parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static com.example.transport.GameParams parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static com.example.transport.GameParams parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static com.example.transport.GameParams parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(com.example.transport.GameParams prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code base.GameParams}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        com.example.transport.GameParams, Builder> implements
      // @@protoc_insertion_point(builder_implements:base.GameParams)
      com.example.transport.GameParamsOrBuilder {
    // Construct using com.example.transport.GameParams.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>uint32 rows = 1;</code>
     * @return The rows.
     */
    @java.lang.Override
    public int getRows() {
      return instance.getRows();
    }
    /**
     * <code>uint32 rows = 1;</code>
     * @param value The rows to set.
     * @return This builder for chaining.
     */
    public Builder setRows(int value) {
      copyOnWrite();
      instance.setRows(value);
      return this;
    }
    /**
     * <code>uint32 rows = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearRows() {
      copyOnWrite();
      instance.clearRows();
      return this;
    }

    /**
     * <code>uint32 cols = 2;</code>
     * @return The cols.
     */
    @java.lang.Override
    public int getCols() {
      return instance.getCols();
    }
    /**
     * <code>uint32 cols = 2;</code>
     * @param value The cols to set.
     * @return This builder for chaining.
     */
    public Builder setCols(int value) {
      copyOnWrite();
      instance.setCols(value);
      return this;
    }
    /**
     * <code>uint32 cols = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearCols() {
      copyOnWrite();
      instance.clearCols();
      return this;
    }

    /**
     * <code>uint32 win = 3;</code>
     * @return The win.
     */
    @java.lang.Override
    public int getWin() {
      return instance.getWin();
    }
    /**
     * <code>uint32 win = 3;</code>
     * @param value The win to set.
     * @return This builder for chaining.
     */
    public Builder setWin(int value) {
      copyOnWrite();
      instance.setWin(value);
      return this;
    }
    /**
     * <code>uint32 win = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearWin() {
      copyOnWrite();
      instance.clearWin();
      return this;
    }

    /**
     * <code>.base.MarkType mark = 4;</code>
     * @return The enum numeric value on the wire for mark.
     */
    @java.lang.Override
    public int getMarkValue() {
      return instance.getMarkValue();
    }
    /**
     * <code>.base.MarkType mark = 4;</code>
     * @param value The mark to set.
     * @return This builder for chaining.
     */
    public Builder setMarkValue(int value) {
      copyOnWrite();
      instance.setMarkValue(value);
      return this;
    }
    /**
     * <code>.base.MarkType mark = 4;</code>
     * @return The mark.
     */
    @java.lang.Override
    public com.example.transport.MarkType getMark() {
      return instance.getMark();
    }
    /**
     * <code>.base.MarkType mark = 4;</code>
     * @param value The enum numeric value on the wire for mark to set.
     * @return This builder for chaining.
     */
    public Builder setMark(com.example.transport.MarkType value) {
      copyOnWrite();
      instance.setMark(value);
      return this;
    }
    /**
     * <code>.base.MarkType mark = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearMark() {
      copyOnWrite();
      instance.clearMark();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:base.GameParams)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new com.example.transport.GameParams();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "rows_",
            "cols_",
            "win_",
            "mark_",
          };
          java.lang.String info =
              "\u0000\u0004\u0000\u0000\u0001\u0004\u0004\u0000\u0000\u0000\u0001\u000b\u0002\u000b" +
              "\u0003\u000b\u0004\f";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<com.example.transport.GameParams> parser = PARSER;
        if (parser == null) {
          synchronized (com.example.transport.GameParams.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<com.example.transport.GameParams>(
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


  // @@protoc_insertion_point(class_scope:base.GameParams)
  private static final com.example.transport.GameParams DEFAULT_INSTANCE;
  static {
    GameParams defaultInstance = new GameParams();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      GameParams.class, defaultInstance);
  }

  public static com.example.transport.GameParams getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<GameParams> PARSER;

  public static com.google.protobuf.Parser<GameParams> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}
