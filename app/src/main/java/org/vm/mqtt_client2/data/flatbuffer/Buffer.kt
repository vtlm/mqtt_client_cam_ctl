package org.vm.mqtt_client2.data.flatbuffer

import com.google.flatbuffers.Constants
import com.google.flatbuffers.FlatBufferBuilder
import com.google.flatbuffers.Table
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Suppress("unused")
class Buffer : Table() {

    fun __init(_i: Int, _bb: ByteBuffer)  {
        __reset(_i, _bb)
    }
    fun __assign(_i: Int, _bb: ByteBuffer) : Buffer {
        __init(_i, _bb)
        return this
    }
    val param1 : Short
        get() {
            val o = __offset(4)
            return if(o != 0) bb.getShort(o + bb_pos) else 0
        }
    companion object {
        fun validateVersion() = Constants.FLATBUFFERS_25_2_10()
        fun getRootAsBuffer(_bb: ByteBuffer): Buffer = getRootAsBuffer(_bb, Buffer())
        fun getRootAsBuffer(_bb: ByteBuffer, obj: Buffer): Buffer {
            _bb.order(ByteOrder.LITTLE_ENDIAN)
            return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb))
        }
        fun createBuffer(builder: FlatBufferBuilder, param1: Short) : Int {
            builder.startTable(1)
            addParam1(builder, param1)
            return endBuffer(builder)
        }
        fun startBuffer(builder: FlatBufferBuilder) = builder.startTable(1)
        fun addParam1(builder: FlatBufferBuilder, param1: Short) = builder.addShort(0, param1, 0)
        fun endBuffer(builder: FlatBufferBuilder) : Int {
            val o = builder.endTable()
            return o
        }
    }
}