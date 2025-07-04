package org.vm.mqtt_client2.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewModelScope
import info.mqtt.android.service.MqttAndroidClient
import info.mqtt.android.service.QoS
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

val NOTIFICATION_ID = 0x335577;

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


    private var mqttAndroidClient: MqttAndroidClient = MqttAndroidClient(applicationContext, SERVER_URI, clientId)
    val topicHandler = TopicHandler()

    init {
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {

            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                if (reconnect) {
                    reConnectCnt += 1
                    addToHistory("Reconnected: $serverURI")
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
                    addToHistory("Connected: $serverURI")
                    appViewModel._mqttStatus.value = "Connected";
                    notify(applicationContext,"MQTT Cam","MQTT connected")
                }
            }

            override fun connectionLost(cause: Throwable?) {
                addToHistory("The Connection was lost.")

                appViewModel._mqttStatus.value = "Disconnected";
                notify(applicationContext,"MQTT Cam","MQTT Diconnected")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                topicHandler.handle(topic, message)
//                receivedMessageHandler(topic, message)

//                val data = String(message.payload, charset("UTF-8"))
//                Log.d("MQTT_D", "arrived: $topic $data")
//                addToHistory("Incoming message: " + String(message.payload).length)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) = Unit
        })

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false

        addToHistory("Connecting: $SERVER_URI")

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
                        addToHistory("Failed to connect: $SERVER_URI")

                        connectAttemptCnt += 1
                        appViewModel._mqttStatus.value = "MQTT Broker Connect Attempts: $connectAttemptCnt"
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

    fun subscribeToTopic(topic: String, qos: Int) {
        mqttAndroidClient.subscribe(topic, qos, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                addToHistory("Subscribed! $SUBSCRIPTION_TOPIC")
//                publishMessage("CamCtl", "getFrame")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                addToHistory("Failed to subscribe $exception")
            }
        })

        // THIS DOES NOT WORK!
//        mqttAndroidClient.subscribe(SUBSCRIPTION_TOPIC, QoS.AtMostOnce.value) { topic, message ->
//            Timber.d("Message arrived $topic : ${String(message.payload)}")
//            addToHistory("Message arrived $message")
//        }
    }

    fun publishMessage(topic: String, message: String) {
        val mqttMessage = MqttMessage()
        mqttMessage.payload = message.toByteArray(charset("UTF-8"))
        mqttMessage.isRetained = false
        if (mqttAndroidClient.isConnected) {
            mqttAndroidClient.publish(topic, mqttMessage)
            addToHistory("Message Published >$message<")
            if (!mqttAndroidClient.isConnected) {
                addToHistory(mqttAndroidClient.bufferedMessageCount.toString() + " messages in buffer.")
            }
        } else {
//            Snackbar.make(findViewById(android.R.id.content), "Not connected", Snackbar.LENGTH_SHORT).setAction("Action", null).show()
        }
    }

//    private fun addToHistory(str: String){
////        Timber.tag("MQTT_D").d(str)
//        Log.d("MQTT_D",str)
//    }

    companion object {
                private const val SERVER_URI = "tcp://375333526167.dyndns.mts.by:1883"
//        private const val SERVER_URI = "tcp://192.168.0.7:1883"
        private const val SUBSCRIPTION_TOPIC = "CamFrame"
        private const val PUBLISH_TOPIC = "exampleAndroidPublishTopic"
        private const val PUBLISH_MESSAGE = "Hello World"
        private var clientId = "ExampleClientPub_378"//"BasicSample" + System.currentTimeMillis()
    }


    fun disconnect(){
        mqttAndroidClient.disconnect()
    }
}


fun addToHistory(str: String){
//        Timber.tag("MQTT_D").d(str)
    Log.d("MQTT_D",str)
}
