//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: base.proto

package com.example.transport;

@kotlin.jvm.JvmName("-initializewinLine")
public inline fun winLine(block: com.example.transport.WinLineKt.Dsl.() -> kotlin.Unit): com.example.transport.WinLine =
  com.example.transport.WinLineKt.Dsl._create(com.example.transport.WinLine.newBuilder()).apply { block() }._build()
public object WinLineKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: com.example.transport.WinLine.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: com.example.transport.WinLine.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): com.example.transport.WinLine = _builder.build()

    /**
     * <code>.base.MarkType mark = 1;</code>
     */
    public var mark: com.example.transport.MarkType
      @JvmName("getMark")
      get() = _builder.getMark()
      @JvmName("setMark")
      set(value) {
        _builder.setMark(value)
      }
    /**
     * <code>.base.MarkType mark = 1;</code>
     */
    public fun clearMark() {
      _builder.clearMark()
    }

    /**
     * <code>optional .base.Move start = 2;</code>
     */
    public var start: com.example.transport.Move
      @JvmName("getStart")
      get() = _builder.getStart()
      @JvmName("setStart")
      set(value) {
        _builder.setStart(value)
      }
    /**
     * <code>optional .base.Move start = 2;</code>
     */
    public fun clearStart() {
      _builder.clearStart()
    }
    /**
     * <code>optional .base.Move start = 2;</code>
     * @return Whether the start field is set.
     */
    public fun hasStart(): kotlin.Boolean {
      return _builder.hasStart()
    }
    public val WinLineKt.Dsl.startOrNull: com.example.transport.Move?
      get() = _builder.startOrNull

    /**
     * <code>optional .base.Move end = 3;</code>
     */
    public var end: com.example.transport.Move
      @JvmName("getEnd")
      get() = _builder.getEnd()
      @JvmName("setEnd")
      set(value) {
        _builder.setEnd(value)
      }
    /**
     * <code>optional .base.Move end = 3;</code>
     */
    public fun clearEnd() {
      _builder.clearEnd()
    }
    /**
     * <code>optional .base.Move end = 3;</code>
     * @return Whether the end field is set.
     */
    public fun hasEnd(): kotlin.Boolean {
      return _builder.hasEnd()
    }
    public val WinLineKt.Dsl.endOrNull: com.example.transport.Move?
      get() = _builder.endOrNull
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun com.example.transport.WinLine.copy(block: com.example.transport.WinLineKt.Dsl.() -> kotlin.Unit): com.example.transport.WinLine =
  com.example.transport.WinLineKt.Dsl._create(this.toBuilder()).apply { block() }._build()

public val com.example.transport.WinLineOrBuilder.startOrNull: com.example.transport.Move?
  get() = if (hasStart()) getStart() else null

public val com.example.transport.WinLineOrBuilder.endOrNull: com.example.transport.Move?
  get() = if (hasEnd()) getEnd() else null
