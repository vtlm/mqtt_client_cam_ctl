package org.vm.mqtt_client2.data

import android.content.Context
import android.util.Log
import info.mqtt.android.service.MqttAndroidClient
import info.mqtt.android.service.QoS
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

class MQTTClient (applicationContext: Context,
                  private val topicsList: List<Pair<String, Int>>,
                  val receivedMessageHandler: (topic: String, message: MqttMessage) -> Unit
){

    var isConnected = false

    private lateinit var mqttAndroidClient: MqttAndroidClient

    init{
        mqttAndroidClient = MqttAndroidClient(applicationContext, SERVER_URI, clientId)
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                if (reconnect) {
                    addToHistory("Reconnected: $serverURI")
                    // Because Clean Session is true, we need to re-subscribe

                    topicsList.forEach {
                        val (topic, qos) = it
                        subscribeToTopic(topic, qos)
                    }

                    isConnected = true

                } else {
                    addToHistory("Connected: $serverURI")
                }
            }

            override fun connectionLost(cause: Throwable?) {
                addToHistory("The Connection was lost.")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {

                receivedMessageHandler(topic, message)

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

        mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                val disconnectedBufferOptions = DisconnectedBufferOptions().apply {
                    isBufferEnabled = true
                    bufferSize = 100
                    isPersistBuffer = false
                    isDeleteOldestMessages = false
                }
                mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)

                topicsList.forEach {
                    val (topic, qos) = it
                    subscribeToTopic(topic, qos)
                }
                isConnected = true
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                addToHistory("Failed to connect: $SERVER_URI")
            }
        })

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

    private fun addToHistory(str: String){
//        Timber.tag("MQTT_D").d(str)
        Log.d("MQTT_D",str)
    }

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