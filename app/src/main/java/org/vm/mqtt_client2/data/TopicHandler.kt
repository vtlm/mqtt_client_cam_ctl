package org.vm.mqtt_client2.data

import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage

class TopicHandler() {

    val topicsHandlers = mutableMapOf<List<Pair<String, Int>>, (MqttMessage) -> Unit> ()

    fun add(topicList: List<Pair<String, Int>>, handler: (MqttMessage) -> Unit){
        topicsHandlers[topicList] = handler
    }

    fun addAndSubscribe(mqttAndroidClient: MqttAndroidClient, topicList: List<Pair<String, Int>>, handler: (MqttMessage) -> Unit){
        add(topicList,handler)
        subscribe(mqttAndroidClient, topicList)
    }

    fun subscribe(mqttAndroidClient: MqttAndroidClient, topicList: List<Pair<String, Int>>){
        topicList.forEach {
            val (topic, qos) = it
            mqttAndroidClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
//                    addToHistory("Subscribed! $SUBSCRIPTION_TOPIC")
//                publishMessage("CamCtl", "getFrame")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
//                    addToHistory("Failed to subscribe $exception")
                }
            })
        }
    }

    fun subscribe(mqttAndroidClient: MqttAndroidClient){
        topicsHandlers.keys.forEach{ subscribe(mqttAndroidClient, it) }
    }

    fun handle(topic: String, message: MqttMessage){
        topicsHandlers.filter { it.key.any { el -> el.first == topic } }.values.forEach{it(message)}
    }

}