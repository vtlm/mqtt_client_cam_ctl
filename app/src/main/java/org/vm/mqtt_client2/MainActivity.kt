package org.vm.mqtt_client2

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import info.mqtt.android.service.MqttAndroidClient
import info.mqtt.android.service.QoS
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.vm.mqtt_client2.data.MQTTCameraClient
import org.vm.mqtt_client2.ui.theme.Mqtt_client2Theme
import timber.log.Timber

class MainActivity : ComponentActivity() {


    private lateinit var camClient: MQTTCameraClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        camClient = MQTTCameraClient(applicationContext)

        Timber.d("onCreate")



        enableEdgeToEdge()
        setContent {
            Mqtt_client2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        BitmapImage(camClient.jpgImage.collectAsState().value)
                    }
                }
            }
        }
    }







    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }


}


@Composable
fun BitmapImage(bitmap: Bitmap?) {
    if(bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "some useful description",
        )
    }else{
        Text("No bitmap")
    }
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