package com.example.transport.service


import com.example.controllers.models.InterruptCause
import com.example.controllers.models.Interruption
import com.example.transport.StopCause
import com.example.transport.Interruption as PbInterruption
import io.grpc.Metadata
import io.grpc.Status
import io.grpc.protobuf.lite.ProtoLiteUtils

internal fun PbInterruption.keyForProto(): Metadata.Key<PbInterruption> = Metadata.Key.of(
    defaultInstanceForType.javaClass.name + Metadata.BINARY_HEADER_SUFFIX,
    ProtoLiteUtils.metadataMarshaller(defaultInstanceForType)
)

internal fun Metadata.toInterruption(): Interruption {
    val interruption = get(PbInterruption.getDefaultInstance().keyForProto())
    requireNotNull(interruption)
    return Interruption( when (val det = interruption.cause) {
        StopCause.STOP_CAUSE_DISCONNECT -> InterruptCause.Disconnected
        StopCause.STOP_CAUSE_LEAVE -> InterruptCause.Leave
        StopCause.STOP_CAUSE_INVALID_MOVE -> InterruptCause.InvalidMove
        StopCause.STOP_CAUSE_INTERNAL -> InterruptCause.Internal
        else -> throw IllegalStateException("unexpected cause: ${det.name}")
    })
}

/*internal fun Status.toInterruption(): Interruption {
    if (detailsList == null) {
        println(message)
        return Interruption(InterruptCause.Disconnected)
    }
    require(detailsList.isNotEmpty())
    val instance = PbInterruption.getDefaultInstance()
    val result = instance.parserForType.parseFrom(getDetails(0).value)
    return Interruption( when (val det = result.cause) {
        StopCause.STOP_CAUSE_DISCONNECT -> InterruptCause.Disconnected
        StopCause.STOP_CAUSE_LEAVE -> InterruptCause.Leave
        StopCause.STOP_CAUSE_INVALID_MOVE -> InterruptCause.InvalidMove
        StopCause.STOP_CAUSE_INTERNAL -> InterruptCause.Internal
        else -> throw IllegalStateException("unexpected cause: ${det.name}")
    })
}*/


