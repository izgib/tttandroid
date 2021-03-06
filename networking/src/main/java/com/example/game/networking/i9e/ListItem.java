// automatically generated by the FlatBuffers compiler, do not modify

package com.example.game.networking.i9e;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.annotation.Nullable;

@SuppressWarnings("unused")

@javax.annotation.Generated(value = "flatc")
public final class ListItem extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_1_12_0();
    }

    public static ListItem getRootAsListItem(ByteBuffer _bb) {
        return getRootAsListItem(_bb, new ListItem());
    }

    public static ListItem getRootAsListItem(ByteBuffer _bb, ListItem obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public ListItem __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public short ID() {
        int o = __offset(4);
        return o != 0 ? bb.getShort(o + bb_pos) : 0;
    }

    public @Nullable
    com.example.game.networking.i9e.GameParams params() {
        return params(new com.example.game.networking.i9e.GameParams());
    }

    public @Nullable
    com.example.game.networking.i9e.GameParams params(com.example.game.networking.i9e.GameParams obj) {
        int o = __offset(6);
        return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null;
    }

    public static int createListItem(FlatBufferBuilder builder,
                                     short ID,
                                     int paramsOffset) {
        builder.startTable(2);
        ListItem.addParams(builder, paramsOffset);
        ListItem.addID(builder, ID);
        return ListItem.endListItem(builder);
    }

    public static void startListItem(FlatBufferBuilder builder) {
        builder.startTable(2);
    }

    public static void addID(FlatBufferBuilder builder, short ID) {
        builder.addShort(0, ID, 0);
    }

    public static void addParams(FlatBufferBuilder builder, int paramsOffset) {
        builder.addOffset(1, paramsOffset, 0);
    }

    public static int endListItem(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public ListItem get(int j) {
            return get(new ListItem(), j);
        }

        public ListItem get(ListItem obj, int j) {
            return obj.__assign(__indirect(__element(j), bb), bb);
        }
    }
}

