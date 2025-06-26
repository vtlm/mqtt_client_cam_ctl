package org.vm.mqtt_client2

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import info.mqtt.android.service.MqttAndroidClient
import info.mqtt.android.service.QoS
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.vm.mqtt_client2.data.AppViewModel
import org.vm.mqtt_client2.data.MQTTCameraClient
import org.vm.mqtt_client2.ui.theme.Mqtt_client2Theme
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("onCreate")

        enableEdgeToEdge()
        setContent {
            Mqtt_client2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MainScreen()
                    }
                }
            }
        }
    }







    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }

    @Composable
    fun MainScreen(){
        Column {
            Text(appViewModel.mqttStatus.collectAsState().value)
            Checkbox(checked = appViewModel.isOnGuard.collectAsState().value,
                onCheckedChange = {appViewModel.setIsOnGuard(it)} )
            ShowCamClients(appViewModel.camClients.collectAsState().value)
        }

    }


}

@Composable
fun ShowCamClients(camClients: List<MQTTCameraClient>){
    LazyColumn {
        camClients.forEach {
//            val bm = it.jpgImage.value
            item (key = System.identityHashCode(it)){
                CameraScreen(it)
            }
        }
    }

}

@Composable
fun CameraScreen(mqttCameraClient: MQTTCameraClient){
    Box{
        BitmapImage(mqttCameraClient)
        Text(mqttCameraClient.name)
    }
}

@Composable
fun BitmapImage(mqttCameraClient: MQTTCameraClient) {
//  if(mqttCameraClient != null) {
    val bitmap = mqttCameraClient.jpgImage.collectAsState().value

    if(bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "some useful description",
        )
    }else{
        Text("No bitmap")
    }
//                        }else{
//                            Text("Initializing...")
//                        }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Mqtt_client2Theme {
        Greeting("Android")
    }
}