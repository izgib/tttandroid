//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: network.proto

package com.example.transport;

@kotlin.jvm.JvmName("-initializegameFilter")
public inline fun gameFilter(block: com.example.transport.GameFilterKt.Dsl.() -> kotlin.Unit): com.example.transport.GameFilter =
  com.example.transport.GameFilterKt.Dsl._create(com.example.transport.GameFilter.newBuilder()).apply { block() }._build()
public object GameFilterKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: com.example.transport.GameFilter.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: com.example.transport.GameFilter.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): com.example.transport.GameFilter = _builder.build()

    /**
     * <code>.transport.Range rows = 1;</code>
     */
    public var rows: com.example.transport.Range
      @JvmName("getRows")
      get() = _builder.getRows()
      @JvmName("setRows")
      set(value) {
        _builder.setRows(value)
      }
    /**
     * <code>.transport.Range rows = 1;</code>
     */
    public fun clearRows() {
      _builder.clearRows()
    }
    /**
     * <code>.transport.Range rows = 1;</code>
     * @return Whether the rows field is set.
     */
    public fun hasRows(): kotlin.Boolean {
      return _builder.hasRows()
    }

    /**
     * <code>.transport.Range cols = 2;</code>
     */
    public var cols: com.example.transport.Range
      @JvmName("getCols")
      get() = _builder.getCols()
      @JvmName("setCols")
      set(value) {
        _builder.setCols(value)
      }
    /**
     * <code>.transport.Range cols = 2;</code>
     */
    public fun clearCols() {
      _builder.clearCols()
    }
    /**
     * <code>.transport.Range cols = 2;</code>
     * @return Whether the cols field is set.
     */
    public fun hasCols(): kotlin.Boolean {
      return _builder.hasCols()
    }

    /**
     * <code>.transport.Range win = 3;</code>
     */
    public var win: com.example.transport.Range
      @JvmName("getWin")
      get() = _builder.getWin()
      @JvmName("setWin")
      set(value) {
        _builder.setWin(value)
      }
    /**
     * <code>.transport.Range win = 3;</code>
     */
    public fun clearWin() {
      _builder.clearWin()
    }
    /**
     * <code>.transport.Range win = 3;</code>
     * @return Whether the win field is set.
     */
    public fun hasWin(): kotlin.Boolean {
      return _builder.hasWin()
    }

    /**
     * <code>.base.MarkType mark = 4;</code>
     */
    public var mark: com.example.transport.MarkType
      @JvmName("getMark")
      get() = _builder.getMark()
      @JvmName("setMark")
      set(value) {
        _builder.setMark(value)
      }
    /**
     * <code>.base.MarkType mark = 4;</code>
     */
    public fun clearMark() {
      _builder.clearMark()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun com.example.transport.GameFilter.copy(block: com.example.transport.GameFilterKt.Dsl.() -> kotlin.Unit): com.example.transport.GameFilter =
  com.example.transport.GameFilterKt.Dsl._create(this.toBuilder()).apply { block() }._build()

public val com.example.transport.GameFilterOrBuilder.rowsOrNull: com.example.transport.Range?
  get() = if (hasRows()) getRows() else null

public val com.example.transport.GameFilterOrBuilder.colsOrNull: com.example.transport.Range?
  get() = if (hasCols()) getCols() else null

public val com.example.transport.GameFilterOrBuilder.winOrNull: com.example.transport.Range?
  get() = if (hasWin()) getWin() else null
