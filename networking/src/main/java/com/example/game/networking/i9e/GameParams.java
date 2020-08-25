// automatically generated by the FlatBuffers compiler, do not modify

package com.example.game.networking.i9e;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")

@javax.annotation.Generated(value = "flatc")
public final class GameParams extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_1_12_0();
    }

    public static GameParams getRootAsGameParams(ByteBuffer _bb) {
        return getRootAsGameParams(_bb, new GameParams());
    }

    public static GameParams getRootAsGameParams(ByteBuffer _bb, GameParams obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public GameParams __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public short rows() {
        int o = __offset(4);
        return o != 0 ? bb.getShort(o + bb_pos) : 0;
    }

    public short cols() {
        int o = __offset(6);
        return o != 0 ? bb.getShort(o + bb_pos) : 0;
    }

    public short win() {
        int o = __offset(8);
        return o != 0 ? bb.getShort(o + bb_pos) : 0;
    }

    public byte mark() {
        int o = __offset(10);
        return o != 0 ? bb.get(o + bb_pos) : 0;
    }

    public static int createGameParams(FlatBufferBuilder builder,
                                       short rows,
                                       short cols,
                                       short win,
                                       byte mark) {
        builder.startTable(4);
        GameParams.addWin(builder, win);
        GameParams.addCols(builder, cols);
        GameParams.addRows(builder, rows);
        GameParams.addMark(builder, mark);
        return GameParams.endGameParams(builder);
    }

    public static void startGameParams(FlatBufferBuilder builder) {
        builder.startTable(4);
    }

    public static void addRows(FlatBufferBuilder builder, short rows) {
        builder.addShort(0, rows, 0);
    }

    public static void addCols(FlatBufferBuilder builder, short cols) {
        builder.addShort(1, cols, 0);
    }

    public static void addWin(FlatBufferBuilder builder, short win) {
        builder.addShort(2, win, 0);
    }

    public static void addMark(FlatBufferBuilder builder, byte mark) {
        builder.addByte(3, mark, 0);
    }

    public static int endGameParams(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public GameParams get(int j) {
            return get(new GameParams(), j);
        }

        public GameParams get(GameParams obj, int j) {
            return obj.__assign(__indirect(__element(j), bb), bb);
        }
    }
}

