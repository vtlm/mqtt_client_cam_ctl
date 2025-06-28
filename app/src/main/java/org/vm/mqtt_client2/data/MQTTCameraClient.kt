package org.vm.mqtt_client2.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.vm.mqtt_client2.R

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
        mqttClient.publishMessage("CamCtl/152", "getFrame")
    }

    private fun receivedMessageHandler(message: MqttMessage){
        addToHistory("$name: size of bitmap ${message.payload.size}")
        _jpgImage.value = BitmapFactory.decodeByteArray(message.payload, 0, message.payload.size )
        mqttClient.publishMessage("CamCtl/152","getFrame")

        val applicationContext = mqttClient.applicationContext
        var builder = NotificationCompat.Builder(applicationContext, applicationContext.getString(R.string.channel_id))
            .setSmallIcon(R.drawable.baseline_warning_24)
            .setContentTitle("My notification")
            .setContentText("Much longer text that cannot fit one line...")
            .setStyle(
                NotificationCompat.BigTextStyle()
                .bigText("Much longer text that cannot fit one line..."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                // ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                //                                        grantResults: IntArray)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return@with
            }
            // notificationId is a unique int for each notification that you must define.
//            notify(NOTIFICATION_ID, builder.build())
        }

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