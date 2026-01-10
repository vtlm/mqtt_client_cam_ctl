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
import timber.log.Timber
import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

const val SC_STREAM_JPG:Short = 0x01
const val SC_ON_GUARD:Short = 0x02

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

    var _heartBeatCnt = MutableStateFlow<Int>(0)
    val heartBeatCnt: StateFlow<Int> = _heartBeatCnt.asStateFlow()

    val _jpgImage = MutableStateFlow<Bitmap?>(null)
    val jpgImage: StateFlow<Bitmap?> = _jpgImage.asStateFlow()

    var requestFrame = false
    var timeOutCnt = 0
    var timeOutMaxCnt = 5
    var framesCnt = 0
    var cnt: Short = 0
    val builder = FlatBufferBuilder(128)


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
    fun setOnGuard(value: Boolean){
        _isOnGuard.value = value
        config = if(value){
            config or SC_ON_GUARD
        }else{
            config and SC_ON_GUARD.inv()
        }
        sendConfig()
//        mqttClient.publishMessage("SetStreamingJpg", isOnGuard.value.toString())
    }

    private fun heartBeatHandler(message: MqttMessage){
        val statusMessage: StatusMessage = StatusMessage.getRootAsStatusMessage(ByteBuffer.wrap(message.payload))
        _heartBeatCnt.value = statusMessage.counter.toInt()
        Timber.tag("DBG_HB").d("Heartbeat cnt ${statusMessage.counter}, status: ${statusMessage.status}")

        if(appViewModel.appState == 0){
            Timber.tag("STATE").d("HB in paused")
            notify(mqttClient.applicationContext,
                "MQTT Cam",
                "MQTT ${statusMessage.counter} times"
            )

        }
    }

    private fun receivedImageHandler(message: MqttMessage){
        Timber.tag("MQTT_D").d("$deviceName:$addressId size of bitmap ${message.payload.size}")
        _jpgImage.value = BitmapFactory.decodeByteArray(message.payload, 0, message.payload.size )
//        mqttClient.publishMessage("CamCtl/152","getFrame")
//        requestFrame = true
        timeOutCnt = 0
        framesCnt++
    }

    fun packBufferParam1(param1: Short){
        builder.clear()
        Buffer.startBuffer(builder)
        Buffer.addParam1(builder, param1)
        val bufferOut= Buffer.endBuffer(builder)
        builder.finish(bufferOut)
    }
    @Deprecated("obsolete")
    fun sendRequest(){
        packBufferParam1(cnt++)
//        val bindta = ByteBuffer.wrap(builder.sizedByteArray())
        mqttClient.publishMessage("$deviceName/$addressId/CamCtl", builder.sizedByteArray(), 2)
    }

    fun sendConfig(){
        packBufferParam1(config)
        mqttClient.publishMessage("$deviceName/$addressId/CamCtl", builder.sizedByteArray(), 2)
    }

    fun checkTimeOut(): Boolean{
        timeOutCnt+=1
        return timeOutCnt >= timeOutMaxCnt
    }


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