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
public final class CrRequest extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_1_12_0();
    }

    public static CrRequest getRootAsCrRequest(ByteBuffer _bb) {
        return getRootAsCrRequest(_bb, new CrRequest());
    }

    public static CrRequest getRootAsCrRequest(ByteBuffer _bb, CrRequest obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public CrRequest __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public byte reqType() {
        int o = __offset(4);
        return o != 0 ? bb.get(o + bb_pos) : 0;
    }

    public @Nullable
    Table req(Table obj) {
        int o = __offset(6);
        return o != 0 ? __union(obj, o + bb_pos) : null;
    }

    public static int createCrRequest(FlatBufferBuilder builder,
                                      byte req_type,
                                      int reqOffset) {
        builder.startTable(2);
        CrRequest.addReq(builder, reqOffset);
        CrRequest.addReqType(builder, req_type);
        return CrRequest.endCrRequest(builder);
    }

    public static void startCrRequest(FlatBufferBuilder builder) {
        builder.startTable(2);
    }

    public static void addReqType(FlatBufferBuilder builder, byte reqType) {
        builder.addByte(0, reqType, 0);
    }

    public static void addReq(FlatBufferBuilder builder, int reqOffset) {
        builder.addOffset(1, reqOffset, 0);
    }

    public static int endCrRequest(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public CrRequest get(int j) {
            return get(new CrRequest(), j);
        }

        public CrRequest get(CrRequest obj, int j) {
            return obj.__assign(__indirect(__element(j), bb), bb);
        }
    }
}

