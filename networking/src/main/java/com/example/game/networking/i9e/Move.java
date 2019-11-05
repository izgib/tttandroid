// automatically generated by the FlatBuffers compiler, do not modify

package com.example.game.networking.i9e;

import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")

@javax.annotation.Generated(value = "flatc")
public final class Move extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_1_11_1();
    }

    public static Move getRootAsMove(ByteBuffer _bb) {
        return getRootAsMove(_bb, new Move());
    }

    public static Move getRootAsMove(ByteBuffer _bb, Move obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public Move __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public short row() {
        int o = __offset(4);
        return o != 0 ? bb.getShort(o + bb_pos) : 0;
    }

    public short col() {
        int o = __offset(6);
        return o != 0 ? bb.getShort(o + bb_pos) : 0;
    }

    public static int createMove(FlatBufferBuilder builder,
                                 short row,
                                 short col) {
        builder.startTable(2);
        Move.addCol(builder, col);
        Move.addRow(builder, row);
        return Move.endMove(builder);
    }

    public static void startMove(FlatBufferBuilder builder) {
        builder.startTable(2);
    }

    public static void addRow(FlatBufferBuilder builder, short row) {
        builder.addShort(0, row, 0);
    }

    public static void addCol(FlatBufferBuilder builder, short col) {
        builder.addShort(1, col, 0);
    }

    public static int endMove(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }
}

