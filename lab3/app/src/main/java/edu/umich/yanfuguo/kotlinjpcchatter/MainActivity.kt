package edu.umich.yanfuguo.kotlinjpcchatter

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import edu.umich.yanfuguo.kotlinjpcchatter.ChattStore.getChatts
import edu.umich.yanfuguo.kotlinjpcchatter.ui.theme.KotlinJpCChatterTheme

class MainActivity : ComponentActivity() {
    private lateinit var audioPlayer: AudioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getChatts(applicationContext){}

        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "MainView") {
                composable("MainView") {
                    MainView(this@MainActivity, navController, audioPlayer)
                }
                composable("PostView") {
                    PostView(this@MainActivity, navController, audioPlayer)
                }
                // passing an optional, nullable argument
                composable("AudioView?autoPlay={autoPlay}",
                    arguments = listOf(navArgument("autoPlay") {
                        type = NavType.BoolType
                        defaultValue = false
                    })) {
                    AudioView(navController, audioPlayer, it.arguments?.getBoolean("autoPlay"))
                }
            }
        }
        val contract = ActivityResultContracts.RequestPermission()
        val recAudioPermissionRequest = registerForActivityResult(contract){ granted ->
            if (!granted) {
                toast("Audio access denied")
                finish()
            }
        }
        recAudioPermissionRequest.launch(Manifest.permission.RECORD_AUDIO)

        // if we’ve previously saved audioPlayer as an instance state prior
        // to orientation change, retrieve and restore it. Otherwise, create
        // a temporary file to hold recorded audio, or audio to be played back,
        // and create a new instance of AudioPlayer:
        savedInstanceState?.run {
            audioPlayer = getParcelable("AUDIOPLAYER")!!
        } ?: externalCacheDir?.let {
            audioPlayer = AudioPlayer(this, "${it.absolutePath}/chatteraudio.m4a")
        } ?: run {
            Log.e("AudioActivity", "external cache dir null")
            toast("Cannot create temporary file!")
            finish()
        }
        // Since we restore the savedInstanceState in onCreate(), we don’t need
        // to provide onRestoreInstanceState() to be called after onStart().

//        setContent {
//            KotlinJpCChatterTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(color = MaterialTheme.colors.background) {
//                    Greeting("Android")
//                }
//            }
//        }
    }
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putParcelable("AUDIOPLAYER", audioPlayer)
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KotlinJpCChatterTheme {
        Greeting("Android")
    }
}