// automatically generated by the FlatBuffers compiler, do not modify

package com.example.transport;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public final class GamesList extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_1_12_0(); }
  public static GamesList getRootAsGamesList(ByteBuffer _bb) { return getRootAsGamesList(_bb, new GamesList()); }
  public static GamesList getRootAsGamesList(ByteBuffer _bb, GamesList obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public GamesList __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public com.example.transport.ListItem items(int j) { return items(new com.example.transport.ListItem(), j); }
  public com.example.transport.ListItem items(com.example.transport.ListItem obj, int j) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int itemsLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public com.example.transport.ListItem.Vector itemsVector() { return itemsVector(new com.example.transport.ListItem.Vector()); }
  public com.example.transport.ListItem.Vector itemsVector(com.example.transport.ListItem.Vector obj) { int o = __offset(4); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }

  public static int createGamesList(FlatBufferBuilder builder,
      int itemsOffset) {
    builder.startTable(1);
    GamesList.addItems(builder, itemsOffset);
    return GamesList.endGamesList(builder);
  }

  public static void startGamesList(FlatBufferBuilder builder) { builder.startTable(1); }
  public static void addItems(FlatBufferBuilder builder, int itemsOffset) { builder.addOffset(0, itemsOffset, 0); }
  public static int createItemsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startItemsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endGamesList(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public GamesList get(int j) { return get(new GamesList(), j); }
    public GamesList get(GamesList obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}
