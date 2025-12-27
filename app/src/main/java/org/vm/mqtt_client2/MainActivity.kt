package org.vm.mqtt_client2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vm.mqtt_client2.data.AppViewModel
import org.vm.mqtt_client2.data.MQTTCameraClient
import org.vm.mqtt_client2.ui.theme.Mqtt_client2Theme
import timber.log.Timber
import kotlin.system.exitProcess

val CHANNEL_ID = "SECURITY_NOTIFY"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    private var _statusString = MutableStateFlow("")
    private val statusString = _statusString.asStateFlow()

    private var _permissionsGranted = MutableStateFlow(false)
    private val permissionsGranted = _permissionsGranted.asStateFlow()

    private var isReCheckPermissions = false

    private val neededPermissions:  MutableList<String> = mutableListOf("")

    @RequiresApi(Build.VERSION_CODES.Q)
    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("onCreate")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val CHANNEL_ID = "SECURITY_NOTIFY"
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        checkPermissions()

        lifecycle.addObserver(appViewModel)

        enableEdgeToEdge()
        setContent {
            Mqtt_client2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MainU(permissionsGranted.collectAsState().value)
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }

    //todo: to coroutine
    @RequiresApi(Build.VERSION_CODES.Q)
    private val reCheckPermissions =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            //Log.d("DPR","in afterPermissions $result")
//            checkPermissions()
//            triggerRestart(this)
            if(result.resultCode == RESULT_OK){
                result.data?.data?.also {
                    Timber.tag("DPR").d("afterPermissions")
                }
            }
        }


    // Register the permissions callback, which handles the user's response to the
// system permissions dialog. Save the return value, an instance of
// ActivityResultLauncher. You can use either a val, as shown in this snippet,
// or a lateinit var in your onAttach() or onCreate() method.
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            neededPermissions.clear()
            _permissionsGranted.value = true
            permissions.entries.forEach {
                Timber.tag("DEBUG").i("${it.key} = ${it.value}")
                if (it.value) {
                    println("Successful......")

                }else{
                    neededPermissions += it.key
                    _permissionsGranted.value = false
                }
            }

            if(_permissionsGranted.value){
//                mediaViewModel.setUserScrollPos(0)
//                lifecycle.addObserver(mediaViewModel)
            }

            _statusString.value = ""
        }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissions(){

        _statusString.value = resources.getString(R.string.checking_permissions)

        val requiredPermissions: MutableList<String> = mutableListOf()

        checkIsPermissionGranted(android.Manifest.permission.POST_NOTIFICATIONS, requiredPermissions)
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
//            checkIsPermissionGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE, requiredPermissions)
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            checkIsPermissionGranted(android.Manifest.permission.READ_MEDIA_AUDIO, requiredPermissions)
//        }
        requiredPermissions += android.Manifest.permission.POST_NOTIFICATIONS;
        requestPermissionLauncher.launch(requiredPermissions.toTypedArray())

    }

    private fun checkIsPermissionGranted(permission: String, collector: MutableList<String>){
        if(ContextCompat.checkSelfPermission(applicationContext, permission) == -1){
            collector += permission
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 1)
    @RequiresApi(Build.VERSION_CODES.Q)
    @Composable
    fun MainU(permissionsGranted: Boolean){
        ShowProgressOrContent(statusString.collectAsState().value) {
            when(permissionsGranted) {
                false -> {
                    RequestPermissionsScreen()
                }
                true -> {
                    //Nav()
                    MainScreen()
//                    ShowClientText(appViewModel.strList.collectAsState().value)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @Composable
    fun RequestPermissionsScreen(){
        Column {
            Row(Modifier.weight(1f)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                ) {

                    Text(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        text = resources.getString(R.string.wait_for_permissions)
                    )
                }
            }
            Row(Modifier.weight(1f)){
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                ) {
                    Column {
                        neededPermissions.forEachIndexed{ ind, str ->
                            val permissionInfo = applicationContext.packageManager.getPermissionInfo(str, PackageManager.GET_META_DATA)
                            val description = permissionInfo.loadDescription(applicationContext.packageManager)
                            Text(text = "${ind + 1}. ${description.toString()}",
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

            }
            Row(Modifier.weight(1f)){
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                ) {

                    Column(Modifier.fillMaxWidth(),
//                    Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally){
                        Button(onClick = {
                            isReCheckPermissions = true
//                                checkPermissions()
                            reCheckPermissions.launch(Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", packageName, null)
                            })
                        },
                            Modifier.fillMaxWidth(0.9f)
                                .padding(vertical = 4.dp)) {
                            Text(text = resources.getString(R.string.Go_To_Settings))
                        }
                        Button(onClick = {
//                                finishAffinity();
                            finishAndRemoveTask()
                            exitProcess(0)
                        },
                            modifier = Modifier.padding(vertical = 4.dp))
                        { Text(text = resources.getString(R.string.Exit))}
                    }
                }

            }
        }

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
fun ShowClientText(strList:List<String>){
    LazyColumn {
        strList.forEachIndexed { ind, it ->
            item (key = ind, (it)){
                Text("$ind, $it")
            }
        }
    }
}

@Composable
fun ShowCamClients(camClients: List<MQTTCameraClient>){
    LazyColumn {
        camClients.forEachIndexed { ind, it ->
            item (key = ind, (it)){
                CameraScreen(it)
            }
        }
    }
}

@Composable
fun CameraScreen(mqttCameraClient: MQTTCameraClient){
    Box{

        BitmapImage(mqttCameraClient.jpgImage.collectAsState().value)
        Column {
            Text("${mqttCameraClient.deviceName}:${mqttCameraClient.addressId}")
            Row (verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = mqttCameraClient._isOnGuard.collectAsState().value,
                    onCheckedChange = { mqttCameraClient.setOnGuard(it) })
                Text("On Guard")
            }
            Row (verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = mqttCameraClient._isStremingJpg.collectAsState().value,
                    onCheckedChange = { mqttCameraClient.setStreamingJpg(it) })
                Text("Streaming Jpg")
            }
            Row (verticalAlignment = Alignment.CenterVertically) {
                Text("Frame: ${mqttCameraClient.framesCnt}")
            }
        }
    }
}

@Composable
fun BitmapImage(bitmap: Bitmap?) {

    if(bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "some useful description",
        )
    }
}
