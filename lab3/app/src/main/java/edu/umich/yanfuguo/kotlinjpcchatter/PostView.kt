package edu.umich.yanfuguo.kotlinjpcchatter

import android.content.Context
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import edu.umich.yanfuguo.kotlinjpcchatter.ChattStore.postChatt

@Composable
fun PostView(context: Context, navController: NavHostController, audioPlayer: AudioPlayer) {
    val username = stringResource(R.string.username)
    var message by remember { mutableStateOf("Some short sample text.") }
    var enableSend by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        // put the topBar here
        topBar = { TopAppBar(
            title = { Text(text = stringResource(R.string.post), fontSize=20.sp) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() } ) {
                    Icon(painter = painterResource(R.drawable.abc_vector_test),
                        stringResource(R.string.chatter))
                }
            },
            actions = { IconButton(onClick = {
                enableSend = false
                postChatt(context, Chatt(username, message,
                    audio = Base64.encodeToString(audioPlayer.audio, Base64.DEFAULT)))
                navController.popBackStack("MainView", inclusive = false)
            }, enabled = enableSend) {
                Icon(painter = painterResource(android.R.drawable.ic_menu_send), stringResource(R.string.send))
            } }) }
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 0.dp).background(Color(0xffffffff))
        ) {
            Text(
                text = username,
                modifier = Modifier.padding(0.dp, 30.dp, 0.dp, 0.dp).fillMaxWidth(1f),
                textAlign= TextAlign.Center,
                fontSize = 20.sp)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier=Modifier.fillMaxWidth(1f)) {
                TextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.padding(8.dp, 20.dp, 8.dp, 0.dp).fillMaxWidth(.8f),
                    textStyle = TextStyle(fontSize = 17.sp),
                    colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)
                )
                IconButton(
                    onClick = {
                        navController.navigate("AudioView")
                    },
                    modifier = Modifier.padding(end = 4.dp).align(Alignment.Bottom)
                ) {
                    if (audioPlayer.audio.size == 0) {
                        Icon(painter = painterResource(R.drawable.ic_baseline_mic_none_24),
                            contentDescription = stringResource(R.string.audio),
                            modifier = Modifier.scale(1.8f),
                            tint = Color.OpenMic
                        )
                    } else {
                        Icon(painter = painterResource(R.drawable.ic_baseline_mic_24),
                            contentDescription = stringResource(R.string.audio),
                            modifier = Modifier.scale(1.8f),
                            tint = Color.FilledMic
                        )
                    }
                }
            }
        }
    }
}