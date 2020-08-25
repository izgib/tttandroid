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
public final class GameFilter extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_1_12_0();
    }

    public static GameFilter getRootAsGameFilter(ByteBuffer _bb) {
        return getRootAsGameFilter(_bb, new GameFilter());
    }

    public static GameFilter getRootAsGameFilter(ByteBuffer _bb, GameFilter obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public GameFilter __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public @Nullable
    com.example.game.networking.i9e.Range rows() {
        return rows(new com.example.game.networking.i9e.Range());
    }

    public @Nullable
    com.example.game.networking.i9e.Range rows(com.example.game.networking.i9e.Range obj) {
        int o = __offset(4);
        return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null;
    }

    public @Nullable
    com.example.game.networking.i9e.Range cols() {
        return cols(new com.example.game.networking.i9e.Range());
    }

    public @Nullable
    com.example.game.networking.i9e.Range cols(com.example.game.networking.i9e.Range obj) {
        int o = __offset(6);
        return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null;
    }

    public @Nullable
    com.example.game.networking.i9e.Range win() {
        return win(new com.example.game.networking.i9e.Range());
    }

    public @Nullable
    com.example.game.networking.i9e.Range win(com.example.game.networking.i9e.Range obj) {
        int o = __offset(8);
        return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null;
    }

    public byte mark() {
        int o = __offset(10);
        return o != 0 ? bb.get(o + bb_pos) : 0;
    }

    public static int createGameFilter(FlatBufferBuilder builder,
                                       int rowsOffset,
                                       int colsOffset,
                                       int winOffset,
                                       byte mark) {
        builder.startTable(4);
        GameFilter.addWin(builder, winOffset);
        GameFilter.addCols(builder, colsOffset);
        GameFilter.addRows(builder, rowsOffset);
        GameFilter.addMark(builder, mark);
        return GameFilter.endGameFilter(builder);
    }

    public static void startGameFilter(FlatBufferBuilder builder) {
        builder.startTable(4);
    }

    public static void addRows(FlatBufferBuilder builder, int rowsOffset) {
        builder.addOffset(0, rowsOffset, 0);
    }

    public static void addCols(FlatBufferBuilder builder, int colsOffset) {
        builder.addOffset(1, colsOffset, 0);
    }

    public static void addWin(FlatBufferBuilder builder, int winOffset) {
        builder.addOffset(2, winOffset, 0);
    }

    public static void addMark(FlatBufferBuilder builder, byte mark) {
        builder.addByte(3, mark, 0);
    }

    public static int endGameFilter(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public GameFilter get(int j) {
            return get(new GameFilter(), j);
        }

        public GameFilter get(GameFilter obj, int j) {
            return obj.__assign(__indirect(__element(j), bb), bb);
        }
    }
}

