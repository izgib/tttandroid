// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: error_details.proto

package com.example.transport;

public final class ErrorDetails {
  private ErrorDetails() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  /**
   * Protobuf enum {@code rpc_service.InterruptionCause}
   */
  public enum InterruptionCause
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>DISCONNECT = 0;</code>
     */
    DISCONNECT(0),
    /**
     * <code>LEAVE = 1;</code>
     */
    LEAVE(1),
    /**
     * <code>OPP_INVALID_MOVE = 2;</code>
     */
    OPP_INVALID_MOVE(2),
    /**
     * <code>INVALID_MOVE = 3;</code>
     */
    INVALID_MOVE(3),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>DISCONNECT = 0;</code>
     */
    public static final int DISCONNECT_VALUE = 0;
    /**
     * <code>LEAVE = 1;</code>
     */
    public static final int LEAVE_VALUE = 1;
    /**
     * <code>OPP_INVALID_MOVE = 2;</code>
     */
    public static final int OPP_INVALID_MOVE_VALUE = 2;
    /**
     * <code>INVALID_MOVE = 3;</code>
     */
    public static final int INVALID_MOVE_VALUE = 3;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static InterruptionCause valueOf(int value) {
      return forNumber(value);
    }

    public static InterruptionCause forNumber(int value) {
      switch (value) {
        case 0: return DISCONNECT;
        case 1: return LEAVE;
        case 2: return OPP_INVALID_MOVE;
        case 3: return INVALID_MOVE;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<InterruptionCause>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        InterruptionCause> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<InterruptionCause>() {
            public InterruptionCause findValueByNumber(int number) {
              return InterruptionCause.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.example.transport.ErrorDetails.getDescriptor().getEnumTypes().get(0);
    }

    private static final InterruptionCause[] VALUES = values();

    public static InterruptionCause valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private InterruptionCause(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:rpc_service.InterruptionCause)
  }

  public interface InterruptionInfoOrBuilder extends
      // @@protoc_insertion_point(interface_extends:rpc_service.InterruptionInfo)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.rpc_service.InterruptionCause cause = 1;</code>
     */
    int getCauseValue();
    /**
     * <code>.rpc_service.InterruptionCause cause = 1;</code>
     */
    com.example.transport.ErrorDetails.InterruptionCause getCause();
  }
  /**
   * Protobuf type {@code rpc_service.InterruptionInfo}
   */
  public  static final class InterruptionInfo extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:rpc_service.InterruptionInfo)
      InterruptionInfoOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use InterruptionInfo.newBuilder() to construct.
    private InterruptionInfo(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private InterruptionInfo() {
      cause_ = 0;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private InterruptionInfo(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {
              int rawValue = input.readEnum();

              cause_ = rawValue;
              break;
            }
            default: {
              if (!parseUnknownFieldProto3(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.example.transport.ErrorDetails.internal_static_rpc_service_InterruptionInfo_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.example.transport.ErrorDetails.internal_static_rpc_service_InterruptionInfo_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.example.transport.ErrorDetails.InterruptionInfo.class, com.example.transport.ErrorDetails.InterruptionInfo.Builder.class);
    }

    public static final int CAUSE_FIELD_NUMBER = 1;
    private int cause_;
    /**
     * <code>.rpc_service.InterruptionCause cause = 1;</code>
     */
    public int getCauseValue() {
      return cause_;
    }
    /**
     * <code>.rpc_service.InterruptionCause cause = 1;</code>
     */
    public com.example.transport.ErrorDetails.InterruptionCause getCause() {
      @SuppressWarnings("deprecation")
      com.example.transport.ErrorDetails.InterruptionCause result = com.example.transport.ErrorDetails.InterruptionCause.valueOf(cause_);
      return result == null ? com.example.transport.ErrorDetails.InterruptionCause.UNRECOGNIZED : result;
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (cause_ != com.example.transport.ErrorDetails.InterruptionCause.DISCONNECT.getNumber()) {
        output.writeEnum(1, cause_);
      }
      unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (cause_ != com.example.transport.ErrorDetails.InterruptionCause.DISCONNECT.getNumber()) {
        size += com.google.protobuf.CodedOutputStream
          .computeEnumSize(1, cause_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof com.example.transport.ErrorDetails.InterruptionInfo)) {
        return super.equals(obj);
      }
      com.example.transport.ErrorDetails.InterruptionInfo other = (com.example.transport.ErrorDetails.InterruptionInfo) obj;

      boolean result = true;
      result = result && cause_ == other.cause_;
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + CAUSE_FIELD_NUMBER;
      hash = (53 * hash) + cause_;
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static com.example.transport.ErrorDetails.InterruptionInfo parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.example.transport.ErrorDetails.InterruptionInfo parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.example.transport.ErrorDetails.InterruptionInfo parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.example.transport.ErrorDetails.InterruptionInfo parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.example.transport.ErrorDetails.InterruptionInfo parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.example.transport.ErrorDetails.InterruptionInfo parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.example.transport.ErrorDetails.InterruptionInfo parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.example.transport.ErrorDetails.InterruptionInfo parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.example.transport.ErrorDetails.InterruptionInfo parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static com.example.transport.ErrorDetails.InterruptionInfo parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.example.transport.ErrorDetails.InterruptionInfo parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.example.transport.ErrorDetails.InterruptionInfo parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(com.example.transport.ErrorDetails.InterruptionInfo prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code rpc_service.InterruptionInfo}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:rpc_service.InterruptionInfo)
        com.example.transport.ErrorDetails.InterruptionInfoOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.example.transport.ErrorDetails.internal_static_rpc_service_InterruptionInfo_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.example.transport.ErrorDetails.internal_static_rpc_service_InterruptionInfo_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.example.transport.ErrorDetails.InterruptionInfo.class, com.example.transport.ErrorDetails.InterruptionInfo.Builder.class);
      }

      // Construct using com.example.transport.ErrorDetails.InterruptionInfo.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        cause_ = 0;

        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.example.transport.ErrorDetails.internal_static_rpc_service_InterruptionInfo_descriptor;
      }

      @java.lang.Override
      public com.example.transport.ErrorDetails.InterruptionInfo getDefaultInstanceForType() {
        return com.example.transport.ErrorDetails.InterruptionInfo.getDefaultInstance();
      }

      @java.lang.Override
      public com.example.transport.ErrorDetails.InterruptionInfo build() {
        com.example.transport.ErrorDetails.InterruptionInfo result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public com.example.transport.ErrorDetails.InterruptionInfo buildPartial() {
        com.example.transport.ErrorDetails.InterruptionInfo result = new com.example.transport.ErrorDetails.InterruptionInfo(this);
        result.cause_ = cause_;
        onBuilt();
        return result;
      }

      @java.lang.Override
      public Builder clone() {
        return (Builder) super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.example.transport.ErrorDetails.InterruptionInfo) {
          return mergeFrom((com.example.transport.ErrorDetails.InterruptionInfo)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.example.transport.ErrorDetails.InterruptionInfo other) {
        if (other == com.example.transport.ErrorDetails.InterruptionInfo.getDefaultInstance()) return this;
        if (other.cause_ != 0) {
          setCauseValue(other.getCauseValue());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.example.transport.ErrorDetails.InterruptionInfo parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.example.transport.ErrorDetails.InterruptionInfo) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private int cause_ = 0;
      /**
       * <code>.rpc_service.InterruptionCause cause = 1;</code>
       */
      public int getCauseValue() {
        return cause_;
      }
      /**
       * <code>.rpc_service.InterruptionCause cause = 1;</code>
       */
      public Builder setCauseValue(int value) {
        cause_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>.rpc_service.InterruptionCause cause = 1;</code>
       */
      public com.example.transport.ErrorDetails.InterruptionCause getCause() {
        @SuppressWarnings("deprecation")
        com.example.transport.ErrorDetails.InterruptionCause result = com.example.transport.ErrorDetails.InterruptionCause.valueOf(cause_);
        return result == null ? com.example.transport.ErrorDetails.InterruptionCause.UNRECOGNIZED : result;
      }
      /**
       * <code>.rpc_service.InterruptionCause cause = 1;</code>
       */
      public Builder setCause(com.example.transport.ErrorDetails.InterruptionCause value) {
        if (value == null) {
          throw new NullPointerException();
        }
        
        cause_ = value.getNumber();
        onChanged();
        return this;
      }
      /**
       * <code>.rpc_service.InterruptionCause cause = 1;</code>
       */
      public Builder clearCause() {
        
        cause_ = 0;
        onChanged();
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFieldsProto3(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:rpc_service.InterruptionInfo)
    }

    // @@protoc_insertion_point(class_scope:rpc_service.InterruptionInfo)
    private static final com.example.transport.ErrorDetails.InterruptionInfo DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new com.example.transport.ErrorDetails.InterruptionInfo();
    }

    public static com.example.transport.ErrorDetails.InterruptionInfo getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<InterruptionInfo>
        PARSER = new com.google.protobuf.AbstractParser<InterruptionInfo>() {
      @java.lang.Override
      public InterruptionInfo parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new InterruptionInfo(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<InterruptionInfo> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<InterruptionInfo> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public com.example.transport.ErrorDetails.InterruptionInfo getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_rpc_service_InterruptionInfo_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_rpc_service_InterruptionInfo_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\023error_details.proto\022\013rpc_service\"A\n\020In" +
      "terruptionInfo\022-\n\005cause\030\001 \001(\0162\036.rpc_serv" +
      "ice.InterruptionCause*V\n\021InterruptionCau" +
      "se\022\016\n\nDISCONNECT\020\000\022\t\n\005LEAVE\020\001\022\024\n\020OPP_INV" +
      "ALID_MOVE\020\002\022\020\n\014INVALID_MOVE\020\003B\027\n\025com.exa" +
      "mple.transportb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_rpc_service_InterruptionInfo_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_rpc_service_InterruptionInfo_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_rpc_service_InterruptionInfo_descriptor,
        new java.lang.String[] { "Cause", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
