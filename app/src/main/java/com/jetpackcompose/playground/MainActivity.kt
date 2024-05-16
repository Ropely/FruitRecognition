package com.jetpackcompose.playground

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.jetpackcompose.playground.TensorFLowHelper.imageSize
import com.jetpackcompose.playground.ui.theme.JetPackComposePlaygroundTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var isSplashScreen = mutableStateOf(true)

        lifecycleScope.launch(Dispatchers.Default) {
            delay(1000)
            isSplashScreen.value = false
        }

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                isSplashScreen.value
            }
        }

        setContent {
            JetPackComposePlaygroundTheme {

                ImagePicker()

            }

        }
    }

    @Composable
    fun Meniu(){

        val scaffoldState = rememberScaffoldState()
        val scope = rememberCoroutineScope()

        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                AppBar(onNavigationIconClick = {
                    scope.launch {
                        scaffoldState.drawerState.open()
                    }
                }
                )

            },
            drawerContent = {
                DrawerHeader()
                DrawerBody(
                    items = listOf(
                        MenuItem(
                            id = "home",
                            title = "home",
                            contentDescription = "go to home screen",
                            icon = Icons.Default.Home
                        ),
                        MenuItem(
                            id = "settings",
                            title = "settings",
                            contentDescription = "go to settings screen",
                            icon = Icons.Default.Settings
                        ),
                        MenuItem(
                            id = "help",
                            title = "help",
                            contentDescription = "get help",
                            icon = Icons.Default.Info
                        ),
                    ),
                    onItemClick =  {
                        println("Clicked on ${it.title}")
                    }
                )
            }
        ) {

        }
    }


    @Composable
    fun ImagePicker() {
        var photoUri by remember {
            mutableStateOf<Uri?>(null)
        }

        val context = LocalContext.current
        var bitmap by remember {
            mutableStateOf<Bitmap?>(null)
        }
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = {
                photoUri = it
            }
        )

        Scaffold(modifier = Modifier.fillMaxSize()) {
            Meniu()
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                photoUri?.let {
                    if (Build.VERSION.SDK_INT < 28)
                        bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                    else {
                        val source = ImageDecoder.createSource(context.contentResolver, it)
                        bitmap = ImageDecoder.decodeBitmap(
                            source,
                            ImageDecoder.OnHeaderDecodedListener { decoder, info, source ->
                                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                                decoder.isMutableRequired = true
                            })
                    }
                }

                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Image from the gallery",
                        Modifier.size(400.dp)
                    )
                    Spacer(modifier = Modifier.padding(20.dp))

                    val scaledBitmap = Bitmap.createScaledBitmap(it, imageSize, imageSize, false);
                    TensorFLowHelper.classifyImage(scaledBitmap) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(text = "Image is classified as:")
                            Text(text = it, color = Color.White, fontSize = 24.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(20.dp))

                Button(onClick = {
                    launcher.launch("image/*")
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Pick an image")
                }
            }
        }
    }

}