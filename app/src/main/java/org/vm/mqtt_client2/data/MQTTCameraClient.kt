package org.vm.mqtt_client2.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.MqttMessage

class MQTTCameraClient (
    val name: String,
    val mqttClient: MQTTClient
//    context: Context,
//    private val clientuser: String
) {

//    @Inject
//    lateinit var userPreferencesRepository: UserPreferencesRepository

    val _jpgImage = MutableStateFlow<Bitmap?>(null)
    val jpgImage: StateFlow<Bitmap?> = _jpgImage.asStateFlow()

    init{
        mqttClient.subscribe(listOf(Pair("CamFrame",1)), ::receivedMessageHandler)
        mqttClient.publishMessage("CamCtl", "getFrame")
    }

    private fun receivedMessageHandler(message: MqttMessage){
        addToHistory("$name: size of bitmap ${message.payload.size}")
        _jpgImage.value = BitmapFactory.decodeByteArray(message.payload, 0, message.payload.size )
        mqttClient.publishMessage("CamCtl","getFrame")
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