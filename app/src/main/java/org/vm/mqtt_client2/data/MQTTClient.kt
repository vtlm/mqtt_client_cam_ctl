package org.vm.mqtt_client2.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewModelScope
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.vm.mqtt_client2.CHANNEL_ID
import org.vm.mqtt_client2.R
import timber.log.Timber


const val NOTIFICATION_ID = 0x335577;

fun notify(applicationContext: Context, title: String, text: String){
    val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        .setSmallIcon(R.drawable.baseline_warning_24)
        .setContentTitle(title)
        .setContentText(text)
//                        .setStyle(NotificationCompat.BigTextStyle()
//                            .bigText("Much longer text that cannot fit one line..."))
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
        notify(NOTIFICATION_ID, builder.build())
    }

}


class MQTTClient (val applicationContext: Context,
    appViewModel: AppViewModel


//                  private val topicsList: List<Pair<String, Int>>,
//                  val receivedMessageHandler: (topic: String, message: MqttMessage) -> Unit
){


    var isConnected = false
    var reConnectCnt = 0


    private var mqttAndroidClient: MqttAndroidClient = MqttAndroidClient(applicationContext, MQTT_BROKER_SERVER_URI, clientId)
    val topicHandler = TopicHandler()

    init {

        mqttAndroidClient.setCallback(object : MqttCallbackExtended {

            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                if (reconnect) {
                    reConnectCnt += 1
                    Timber.tag("MQTT_D").d("Reconnected: $serverURI")
                    appViewModel._mqttStatus.value = "Connected ($reConnectCnt)";

                    notify(applicationContext,
                        "MQTT Cam",
                        "MQTT reconnected $reConnectCnt times"
                        )
                    // Because Clean Session is true, we need to re-subscribe

//                    topicsList.forEach {
//                        val (topic, qos) = it
//                        subscribeToTopic(topic, qos)
//                    }
                    topicHandler.subscribe(mqttAndroidClient)

                    isConnected = true

                } else {
                    Timber.tag("MQTT_D").d("Connected: $serverURI")
                    appViewModel._mqttStatus.value = "Connected";
                    notify(applicationContext,"MQTT Cam","MQTT connected")
                }
            }

            override fun connectionLost(cause: Throwable?) {
                Timber.tag("MQTT_D").d("The Connection was lost.")

                appViewModel._mqttStatus.value = "Disconnected";
                notify(applicationContext,"MQTT Cam","MQTT Diconnected")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                topicHandler.handle(topic, message)
//                receivedMessageHandler(topic, message)

//                val data = String(message.payload, charset("UTF-8"))
//                Log.d("MQTT_D", "arrived: $topic $data")
//                Timber.tag("MQTT_D").d("Incoming message: " + String(message.payload).length)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) = Unit
        })

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false

        Timber.tag("MQTT_D").d("Connecting: $MQTT_BROKER_SERVER_URI")

//        appViewModel._mqttStatus.value = "Try to Connect"

        appViewModel.viewModelScope.launch {

            var connectAttemptCnt = 0

            while (!isConnected) {
                mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        val disconnectedBufferOptions = DisconnectedBufferOptions().apply {
                            isBufferEnabled = true
                            bufferSize = 100
                            isPersistBuffer = false
                            isDeleteOldestMessages = false
                        }
                        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)

//                topicHandler.subscribe(mqttAndroidClient)

//                topicsList.forEach {
//                    val (topic, qos) = it
//                    subscribeToTopic(topic, qos)
//                }
                        isConnected = true
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Timber.tag("MQTT_D").d("Failed to connect: $MQTT_BROKER_SERVER_URI")

                        connectAttemptCnt += 1
//                        appViewModel._mqttStatus.value = "MQTT Broker Connect Attempts: $connectAttemptCnt"
                    }
                })

                delay(4000)
            }

    }

    }//init

//    fun addTopicHandler()

    fun subscribe(topicList: List<Pair<String, Int>>, handler: (MqttMessage) -> Unit){
        topicHandler.addAndSubscribe(mqttAndroidClient, topicList, handler)
    }

//    fun subscribeToTopic(topic: String, qos: Int) {
//        mqttAndroidClient.subscribe(topic, qos, null, object : IMqttActionListener {
//            override fun onSuccess(asyncActionToken: IMqttToken) {
//                Timber.tag("MQTT_D").d("Subscribed! $SUBSCRIPTION_TOPIC")
////                publishMessage("CamCtl", "getFrame")
//            }
//
//            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
//                Timber.tag("MQTT_D").d("Failed to subscribe $exception")
//            }
//        })
//
//        // THIS DOES NOT WORK!
////        mqttAndroidClient.subscribe(SUBSCRIPTION_TOPIC, QoS.AtMostOnce.value) { topic, message ->
////            Timber.d("Message arrived $topic : ${String(message.payload)}")
////            Timber.tag("MQTT_D").d("Message arrived $message")
////        }
//    }

    fun publishMessage(topic: String, message: ByteArray) {
        val mqttMessage = MqttMessage()
        mqttMessage.payload = message
        mqttMessage.isRetained = false  //as default
//        mqttMessage.id
        mqttMessage.qos = 2  //as default
        if (mqttAndroidClient.isConnected) {
            mqttAndroidClient.publish(topic, mqttMessage)
            Timber.tag("MQTT_D").d("Message Published >$message<")
            if (!mqttAndroidClient.isConnected) {
                Timber.tag("MQTT_D").d("%s messages in buffer.", mqttAndroidClient.bufferedMessageCount.toString())
            }
        } else {
//            Snackbar.make(findViewById(android.R.id.content), "Not connected", Snackbar.LENGTH_SHORT).setAction("Action", null).show()
        }
    }
    fun publishMessage(topic: String, message: String) {
        val mqttMessage = MqttMessage()
        mqttMessage.payload = message.toByteArray(charset("UTF-8"))
        mqttMessage.isRetained = false
        if (mqttAndroidClient.isConnected) {
            mqttAndroidClient.publish(topic, mqttMessage)
            Timber.tag("MQTT_D").d("Topic Published: $topic")
            if (!mqttAndroidClient.isConnected) {
                Timber.tag("MQTT_D").d("%s messages in buffer.", mqttAndroidClient.bufferedMessageCount.toString())
            }
        } else {
//            Snackbar.make(findViewById(android.R.id.content), "Not connected", Snackbar.LENGTH_SHORT).setAction("Action", null).show()
        }
    }


    companion object {
        private const val MQTT_BROKER_SERVER_URI = "tcp://375333526167.dyndns.mts.by:1883"
        private var clientId = "MobileApp"//"BasicSample" + System.currentTimeMillis()
    }


    fun disconnect(){
        mqttAndroidClient.disconnect()
    }
}


//fun Timber.tag("MQTT_D").d(str: String){
////        Timber.tag("MQTT_D").d(str)
//    Timber.tag("MQTT_D").d(str)
//}
