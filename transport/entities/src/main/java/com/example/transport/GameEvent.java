// automatically generated by the FlatBuffers compiler, do not modify

package com.example.transport;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public final class GameEvent extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_1_12_0(); }
  public static GameEvent getRootAsGameEvent(ByteBuffer _bb) { return getRootAsGameEvent(_bb, new GameEvent()); }
  public static GameEvent getRootAsGameEvent(ByteBuffer _bb, GameEvent obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public GameEvent __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public byte type() { int o = __offset(4); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public  @Nullable com.example.transport.WinLine followUp() { return followUp(new com.example.transport.WinLine()); }
  public  @Nullable com.example.transport.WinLine followUp(com.example.transport.WinLine obj) { int o = __offset(6); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }

  public static int createGameEvent(FlatBufferBuilder builder,
      byte type,
      int followUpOffset) {
    builder.startTable(2);
    GameEvent.addFollowUp(builder, followUpOffset);
    GameEvent.addType(builder, type);
    return GameEvent.endGameEvent(builder);
  }

  public static void startGameEvent(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addType(FlatBufferBuilder builder, byte type) { builder.addByte(0, type, 0); }
  public static void addFollowUp(FlatBufferBuilder builder, int followUpOffset) { builder.addOffset(1, followUpOffset, 0); }
  public static int endGameEvent(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public GameEvent get(int j) { return get(new GameEvent(), j); }
    public GameEvent get(GameEvent obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

