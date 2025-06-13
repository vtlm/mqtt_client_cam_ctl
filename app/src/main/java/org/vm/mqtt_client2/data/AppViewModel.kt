package org.vm.mqtt_client2.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel  @Inject constructor(
//    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext val context: Context
): ViewModel() {

    private var mqttClient = MQTTClient(context)
//        TopicHandler(mutableMapOf((listOf(Pair("CamFrame",1)) to ::receivedMessageHandler ))))
//    var mqttCamClient: MQTTCameraClient? = null

    private var _camClients = MutableStateFlow(listOf<MQTTCameraClient>())
    val camClients: StateFlow<List<MQTTCameraClient>> = _camClients.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Default){
            while (!mqttClient.isConnected) {
                delay(100)
            }
            _camClients.value = _camClients.value.plus(MQTTCameraClient(mqttClient))
            _camClients.value = _camClients.value.plus(MQTTCameraClient(mqttClient))
            _camClients.value = _camClients.value.plus(MQTTCameraClient(mqttClient))
        }
    }

    }