package org.vm.mqtt_client2.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject


class SyncTask(val parent: AppViewModel): TimerTask(){
    override fun run() {
//        TODO("Not yet implemented")
        Timber.tag("TMR").d("Running")

    Log.d("L_TMR","execute task")
        parent.run()
    }
}

@HiltViewModel
class AppViewModel  @Inject constructor(
//    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext val context: Context
): ViewModel(), DefaultLifecycleObserver {

    private lateinit var timer: Timer
//    val syncTask = SyncTask(this)
    private var mqttClient = MQTTClient(context, this)
//        TopicHandler(mutableMapOf((listOf(Pair("CamFrame",1)) to ::receivedMessageHandler ))))
//    var mqttCamClient: MQTTCameraClient? = null

    private val _progressTitle = MutableStateFlow("")
    val progressTitle: StateFlow<String> = _progressTitle.asStateFlow()


    var _isOnGuard = MutableStateFlow<Boolean>(false)
    val isOnGuard: StateFlow<Boolean> = _isOnGuard.asStateFlow()
    fun setIsOnGuard(value: Boolean){_isOnGuard.value = value}

    var _mqttStatus = MutableStateFlow<String>("MQTT Status")
    val mqttStatus: StateFlow<String> = _mqttStatus.asStateFlow()

    private var _camClients = MutableStateFlow(listOf<MQTTCameraClient>())
    val camClients: StateFlow<List<MQTTCameraClient>> = _camClients.asStateFlow()

    init {

        //mqttClient.
//        Timber.plant(Timber.DebugTree())
//        Timber.plant(Timber.)

        viewModelScope.launch(Dispatchers.Default){
            while (!mqttClient.isConnected) {
                delay(100)
            }
            _camClients.value = _camClients.value.plus(MQTTCameraClient(this@AppViewModel,"152", mqttClient))
            _camClients.value = _camClients.value.plus(MQTTCameraClient(this@AppViewModel,"152", mqttClient))
            _camClients.value = _camClients.value.plus(MQTTCameraClient(this@AppViewModel,"152", mqttClient))
        }

        _camClients.value.forEach{it.sendRequest()}

//        timer.schedule(syncTask,500,100)
    }


    override fun onCreate(owner: LifecycleOwner) {//override lifecycle events
        super.onCreate(owner)
        //Log.d("VMO","create")
    }
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        //Log.d("VMO","start")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        timer = Timer()
        timer.schedule(SyncTask(this),500,100)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        timer.cancel()
    }

    override fun onStop(owner: LifecycleOwner) {
        //Log.d("VMO","stop")
        super.onStop(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        //Log.d("VMO","destroy")
        super.onDestroy(owner)
    }


    fun run(){
        Log.d("L_TMR","execute task in viewModel")
        _camClients.value.forEach {
            if(it.checkTimeOut()){
                Log.d("L_TMR","timeout ${it.name}")
                it.sendRequest()
            }
        }
    }

    }