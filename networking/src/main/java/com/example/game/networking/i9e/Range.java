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
public final class Range extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_1_12_0();
    }

    public static Range getRootAsRange(ByteBuffer _bb) {
        return getRootAsRange(_bb, new Range());
    }

    public static Range getRootAsRange(ByteBuffer _bb, Range obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public Range __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public short start() {
        int o = __offset(4);
        return o != 0 ? bb.getShort(o + bb_pos) : 3;
    }

    public short end() {
        int o = __offset(6);
        return o != 0 ? bb.getShort(o + bb_pos) : 15;
    }

    public static int createRange(FlatBufferBuilder builder,
                                  short start,
                                  short end) {
        builder.startTable(2);
        Range.addEnd(builder, end);
        Range.addStart(builder, start);
        return Range.endRange(builder);
    }

    public static void startRange(FlatBufferBuilder builder) {
        builder.startTable(2);
    }

    public static void addStart(FlatBufferBuilder builder, short start) {
        builder.addShort(0, start, 3);
    }

    public static void addEnd(FlatBufferBuilder builder, short end) {
        builder.addShort(1, end, 15);
    }

    public static int endRange(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public Range get(int j) {
            return get(new Range(), j);
        }

        public Range get(Range obj, int j) {
            return obj.__assign(__indirect(__element(j), bb), bb);
        }
    }
}

