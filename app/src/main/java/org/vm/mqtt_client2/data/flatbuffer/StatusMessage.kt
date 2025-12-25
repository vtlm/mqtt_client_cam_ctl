package org.vm.mqtt_client2.data.flatbuffer

import com.google.flatbuffers.Constants
import com.google.flatbuffers.FlatBufferBuilder
import com.google.flatbuffers.Table
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Suppress("unused")
class StatusMessage : Table() {

    fun __init(_i: Int, _bb: ByteBuffer)  {
        __reset(_i, _bb)
    }
    fun __assign(_i: Int, _bb: ByteBuffer) : StatusMessage {
        __init(_i, _bb)
        return this
    }
    val counter : UShort
        get() {
            val o = __offset(4)
            return if(o != 0) bb.getShort(o + bb_pos).toUShort() else 0u
        }
    val status : UInt
        get() {
            val o = __offset(6)
            return if(o != 0) bb.getInt(o + bb_pos).toUInt() else 0u
        }
    companion object {
        fun validateVersion() = Constants.FLATBUFFERS_25_2_10()
        fun getRootAsStatusMessage(_bb: ByteBuffer): StatusMessage = getRootAsStatusMessage(_bb, StatusMessage())
        fun getRootAsStatusMessage(_bb: ByteBuffer, obj: StatusMessage): StatusMessage {
            _bb.order(ByteOrder.LITTLE_ENDIAN)
            return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb))
        }
        fun createStatusMessage(builder: FlatBufferBuilder, counter: UShort, status: UInt) : Int {
            builder.startTable(2)
            addStatus(builder, status)
            addCounter(builder, counter)
            return endStatusMessage(builder)
        }
        fun startStatusMessage(builder: FlatBufferBuilder) = builder.startTable(2)
        fun addCounter(builder: FlatBufferBuilder, counter: UShort) = builder.addShort(0, counter.toShort(), 0)
        fun addStatus(builder: FlatBufferBuilder, status: UInt) = builder.addInt(1, status.toInt(), 0)
        fun endStatusMessage(builder: FlatBufferBuilder) : Int {
            val o = builder.endTable()
            return o
        }
    }
}