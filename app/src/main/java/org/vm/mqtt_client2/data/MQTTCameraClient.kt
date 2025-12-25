package org.vm.mqtt_client2.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.flatbuffers.FlatBufferBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.vm.mqtt_client2.data.flatbuffer.Buffer
import org.vm.mqtt_client2.data.flatbuffer.StatusMessage
import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

const val SC_STREAM_JPG:Short = 0x01
//private val MQTTCameraClient.SC_STREAMING_JPG: Short
//    get() = 0x01
class MQTTCameraClient (
    val appViewModel: AppViewModel,
    val deviceName: String,
    val addressId: String,
    val mqttClient: MQTTClient
//    context: Context,
//    private val clientuser: String
) {


    var config:Short=0;
//    @Inject
//    lateinit var userPreferencesRepository: UserPreferencesRepository
    var _isStremingJpg = MutableStateFlow<Boolean>(false)
    val isStreamingJpg: StateFlow<Boolean> = _isStremingJpg.asStateFlow()
    var _isOnGuard = MutableStateFlow<Boolean>(false)
    val isOnGuard: StateFlow<Boolean> = _isOnGuard.asStateFlow()

    val _jpgImage = MutableStateFlow<Bitmap?>(null)
    val jpgImage: StateFlow<Bitmap?> = _jpgImage.asStateFlow()

    var requestFrame = false
    var timeOutCnt = 0
    var timeOutMaxCnt = 5

    var cnt: Short = 0

    init{
        mqttClient.subscribe(listOf(Pair("$deviceName/$addressId/CamFrame",1)), ::receivedImageHandler)
        mqttClient.subscribe(listOf(Pair("$deviceName/$addressId/HeartBeat",1)), ::heartBeatHandler)
//        mqttClient.publishMessage("CamCtl/$name", "getFrame")

//        appViewModel.viewModelScope.launch {
//            while(true){
//                if(requestFrame){
//                    requestFrame = false
//                    mqttClient.publishMessage("CamCtl/$name", "getFrame")
//                }
//            }
//        }
    }

    fun setStreamingJpg(value: Boolean){
        _isStremingJpg.value = value
        config = if(value){
            config or SC_STREAM_JPG
        }else{
            config and SC_STREAM_JPG.inv()
        }
        sendConfig()
//        mqttClient.publishMessage("SetStreamingJpg", isOnGuard.value.toString())
    }
    fun littleEndianBytesToInt(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toInt() and 0xFF shl (8 * i))
        }
        return result
    }

    fun bigEndianBytesToInt(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toInt() and 0xFF shl (8 * (bytes.size - 1 - i)))
        }
        return result
    }

    private fun heartBeatHandler(message: MqttMessage){

        val statusMessage: StatusMessage = StatusMessage.getRootAsStatusMessage(ByteBuffer.wrap(message.payload))
        Log.d("DBG_HB","Heartbeat cnt ${statusMessage.counter}, status: ${statusMessage.status}")
    }

    private fun receivedImageHandler(message: MqttMessage){
        addToHistory("$deviceName:$addressId size of bitmap ${message.payload.size}")
        _jpgImage.value = BitmapFactory.decodeByteArray(message.payload, 0, message.payload.size )
//        mqttClient.publishMessage("CamCtl/152","getFrame")
//        requestFrame = true
        timeOutCnt = 0
    }

    fun sendRequest(){
        val builder = FlatBufferBuilder(1024)
        Buffer.startBuffer(builder)
        Buffer.addParam1(builder, cnt++)
        val buffrOut= Buffer.endBuffer(builder)
        builder.finish(buffrOut)
        val bindta = ByteBuffer.wrap(builder.sizedByteArray())

        mqttClient.publishMessage("$deviceName/$addressId/CamCtl", builder.sizedByteArray())
    }

    fun sendConfig(){
        val builder = FlatBufferBuilder(1024)
        Buffer.startBuffer(builder)
        Buffer.addParam1(builder, config)
        val buffrOut= Buffer.endBuffer(builder)
        builder.finish(buffrOut)
        val bindta = ByteBuffer.wrap(builder.sizedByteArray())

        mqttClient.publishMessage("$deviceName/$addressId/CamCtl", builder.sizedByteArray())
    }

    fun checkTimeOut(): Boolean{
        timeOutCnt+=1
        if(timeOutCnt >= timeOutMaxCnt){
            return true
        }
        return false
    }

//    companion object{
//        const val SC_STREAM_JPG : Short = 0x01
//
//    }

}

//class MQTTCameraClient @Inject constructor(
//    private val clientId: String,
//    private val userPreferencesRepository: UserPreferencesRepository,
//    private val mqttClient: MQTT_Client,
//    @ApplicationContext val context: Context
//) {
//    val _jpgImage = MutableStateFlow<Bitmap?>(null)
//    val jpgImage: StateFlow<Bitmap?> = _jpgImage.asStateFlow()
//
//}