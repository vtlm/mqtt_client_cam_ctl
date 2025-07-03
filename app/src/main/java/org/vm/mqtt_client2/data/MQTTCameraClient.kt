package org.vm.mqtt_client2.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.vm.mqtt_client2.R

class MQTTCameraClient (
    val appViewModel: AppViewModel,
    val name: String,
    val mqttClient: MQTTClient
//    context: Context,
//    private val clientuser: String
) {

//    @Inject
//    lateinit var userPreferencesRepository: UserPreferencesRepository

    val _jpgImage = MutableStateFlow<Bitmap?>(null)
    val jpgImage: StateFlow<Bitmap?> = _jpgImage.asStateFlow()

    var requestFrame = false
    var timeOutCnt = 0
    var timeOutMaxCnt = 5

    init{
        mqttClient.subscribe(listOf(Pair("CamFrame",1)), ::receivedMessageHandler)
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

    private fun receivedMessageHandler(message: MqttMessage){
        addToHistory("$name: size of bitmap ${message.payload.size}")
        _jpgImage.value = BitmapFactory.decodeByteArray(message.payload, 0, message.payload.size )
//        mqttClient.publishMessage("CamCtl/152","getFrame")
//        requestFrame = true
        timeOutCnt = 0
    }

    fun sendRequest(){
        mqttClient.publishMessage("CamCtl/$name","getFrame")
    }

    fun checkTimeOut(): Boolean{
        timeOutCnt+=1
        if(timeOutCnt >= timeOutMaxCnt){
            return true
        }
        return false
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