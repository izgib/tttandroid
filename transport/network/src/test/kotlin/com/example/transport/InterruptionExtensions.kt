package com.example.transport

import com.example.transport.service.keyForProto
import com.google.protobuf.any
import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusRuntimeException


internal fun Interruption.toStatusRuntimeException(): StatusRuntimeException {
    val md = Metadata().apply { put(keyForProto(), this@toStatusRuntimeException) }
    return Status.INTERNAL.asRuntimeException(md)
}


internal fun Interruption.toAnyMessage(): com.google.protobuf.Any {
    return any {
        typeUrl = "/${Interruption.getDefaultInstance()::class.java.name}"
        value = value
    }
}