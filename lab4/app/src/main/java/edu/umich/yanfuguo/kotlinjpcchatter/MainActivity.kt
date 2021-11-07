package edu.umich.yanfuguo.kotlinjpcchatter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.umich.yanfuguo.kotlinjpcchatter.ChattStore.getChatts
import edu.umich.yanfuguo.kotlinjpcchatter.ui.theme.KotlinJpCChatterTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getChatts()

        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "MainView") {
                composable("MainView") {
                    MainView(this@MainActivity, navController)
                }
                composable("PostView") {
                    PostView(this@MainActivity, navController)
                }
                composable("SigninView") {
                    SigninView(this@MainActivity, navController)
                }
            }
        }
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