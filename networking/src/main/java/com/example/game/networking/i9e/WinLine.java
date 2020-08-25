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
public final class WinLine extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_1_12_0();
    }

    public static WinLine getRootAsWinLine(ByteBuffer _bb) {
        return getRootAsWinLine(_bb, new WinLine());
    }

    public static WinLine getRootAsWinLine(ByteBuffer _bb, WinLine obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public WinLine __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public byte mark() {
        int o = __offset(4);
        return o != 0 ? bb.get(o + bb_pos) : 0;
    }

    public @Nullable
    com.example.game.networking.i9e.Move start() {
        return start(new com.example.game.networking.i9e.Move());
    }

    public @Nullable
    com.example.game.networking.i9e.Move start(com.example.game.networking.i9e.Move obj) {
        int o = __offset(6);
        return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null;
    }

    public @Nullable
    com.example.game.networking.i9e.Move end() {
        return end(new com.example.game.networking.i9e.Move());
    }

    public @Nullable
    com.example.game.networking.i9e.Move end(com.example.game.networking.i9e.Move obj) {
        int o = __offset(8);
        return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null;
    }

    public static int createWinLine(FlatBufferBuilder builder,
                                    byte mark,
                                    int startOffset,
                                    int endOffset) {
        builder.startTable(3);
        WinLine.addEnd(builder, endOffset);
        WinLine.addStart(builder, startOffset);
        WinLine.addMark(builder, mark);
        return WinLine.endWinLine(builder);
    }

    public static void startWinLine(FlatBufferBuilder builder) {
        builder.startTable(3);
    }

    public static void addMark(FlatBufferBuilder builder, byte mark) {
        builder.addByte(0, mark, 0);
    }

    public static void addStart(FlatBufferBuilder builder, int startOffset) {
        builder.addOffset(1, startOffset, 0);
    }

    public static void addEnd(FlatBufferBuilder builder, int endOffset) {
        builder.addOffset(2, endOffset, 0);
    }

    public static int endWinLine(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public WinLine get(int j) {
            return get(new WinLine(), j);
        }

        public WinLine get(WinLine obj, int j) {
            return obj.__assign(__indirect(__element(j), bb), bb);
        }
    }
}

