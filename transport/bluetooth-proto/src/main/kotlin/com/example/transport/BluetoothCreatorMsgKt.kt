//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: bluetooth.proto

package com.example.transport;

@kotlin.jvm.JvmName("-initializebluetoothCreatorMsg")
public inline fun bluetoothCreatorMsg(block: com.example.transport.BluetoothCreatorMsgKt.Dsl.() -> kotlin.Unit): com.example.transport.BluetoothCreatorMsg =
  com.example.transport.BluetoothCreatorMsgKt.Dsl._create(com.example.transport.BluetoothCreatorMsg.newBuilder()).apply { block() }._build()
public object BluetoothCreatorMsgKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: com.example.transport.BluetoothCreatorMsg.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: com.example.transport.BluetoothCreatorMsg.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): com.example.transport.BluetoothCreatorMsg = _builder.build()

    /**
     * <code>.base.GameStatus status = 1;</code>
     */
    public var status: com.example.transport.GameStatus
      @JvmName("getStatus")
      get() = _builder.getStatus()
      @JvmName("setStatus")
      set(value) {
        _builder.setStatus(value)
      }
    /**
     * <code>.base.GameStatus status = 1;</code>
     */
    public fun clearStatus() {
      _builder.clearStatus()
    }
    /**
     * <code>.base.GameStatus status = 1;</code>
     * @return Whether the status field is set.
     */
    public fun hasStatus(): kotlin.Boolean {
      return _builder.hasStatus()
    }

    /**
     * <code>.base.WinLine win_line = 2;</code>
     */
    public var winLine: com.example.transport.WinLine
      @JvmName("getWinLine")
      get() = _builder.getWinLine()
      @JvmName("setWinLine")
      set(value) {
        _builder.setWinLine(value)
      }
    /**
     * <code>.base.WinLine win_line = 2;</code>
     */
    public fun clearWinLine() {
      _builder.clearWinLine()
    }
    /**
     * <code>.base.WinLine win_line = 2;</code>
     * @return Whether the winLine field is set.
     */
    public fun hasWinLine(): kotlin.Boolean {
      return _builder.hasWinLine()
    }

    /**
     * <code>.base.StopCause cause = 3;</code>
     */
    public var cause: com.example.transport.StopCause
      @JvmName("getCause")
      get() = _builder.getCause()
      @JvmName("setCause")
      set(value) {
        _builder.setCause(value)
      }
    /**
     * <code>.base.StopCause cause = 3;</code>
     */
    public fun clearCause() {
      _builder.clearCause()
    }
    /**
     * <code>.base.StopCause cause = 3;</code>
     * @return Whether the cause field is set.
     */
    public fun hasCause(): kotlin.Boolean {
      return _builder.hasCause()
    }

    /**
     * <code>.base.GameParams params = 4;</code>
     */
    public var params: com.example.transport.GameParams
      @JvmName("getParams")
      get() = _builder.getParams()
      @JvmName("setParams")
      set(value) {
        _builder.setParams(value)
      }
    /**
     * <code>.base.GameParams params = 4;</code>
     */
    public fun clearParams() {
      _builder.clearParams()
    }
    /**
     * <code>.base.GameParams params = 4;</code>
     * @return Whether the params field is set.
     */
    public fun hasParams(): kotlin.Boolean {
      return _builder.hasParams()
    }

    /**
     * <code>optional .base.Move move = 5;</code>
     */
    public var move: com.example.transport.Move
      @JvmName("getMove")
      get() = _builder.getMove()
      @JvmName("setMove")
      set(value) {
        _builder.setMove(value)
      }
    /**
     * <code>optional .base.Move move = 5;</code>
     */
    public fun clearMove() {
      _builder.clearMove()
    }
    /**
     * <code>optional .base.Move move = 5;</code>
     * @return Whether the move field is set.
     */
    public fun hasMove(): kotlin.Boolean {
      return _builder.hasMove()
    }
    public val BluetoothCreatorMsgKt.Dsl.moveOrNull: com.example.transport.Move?
      get() = _builder.moveOrNull
    public val payloadCase: com.example.transport.BluetoothCreatorMsg.PayloadCase
      @JvmName("getPayloadCase")
      get() = _builder.getPayloadCase()

    public fun clearPayload() {
      _builder.clearPayload()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun com.example.transport.BluetoothCreatorMsg.copy(block: com.example.transport.BluetoothCreatorMsgKt.Dsl.() -> kotlin.Unit): com.example.transport.BluetoothCreatorMsg =
  com.example.transport.BluetoothCreatorMsgKt.Dsl._create(this.toBuilder()).apply { block() }._build()

public val com.example.transport.BluetoothCreatorMsgOrBuilder.winLineOrNull: com.example.transport.WinLine?
  get() = if (hasWinLine()) getWinLine() else null

public val com.example.transport.BluetoothCreatorMsgOrBuilder.paramsOrNull: com.example.transport.GameParams?
  get() = if (hasParams()) getParams() else null

public val com.example.transport.BluetoothCreatorMsgOrBuilder.moveOrNull: com.example.transport.Move?
  get() = if (hasMove()) getMove() else null
