package edu.umich.yanfuguo.kotlinjpcchatter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun ChattListRow(index: Int, chatt: Chatt, navController: NavHostController, audioPlayer: AudioPlayer) {
    chatt.message?.let { Text(it, fontSize = 17.sp, modifier = Modifier.padding(4.dp, 10.dp, 4.dp, 10.dp)) }
    // The content of a ChattListRow composable consists of a Column of
    // two items: a Row on top and, below it, a text box containing the chatt message.
    Column(modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 0.dp)
        .background(color = Color(if (index % 2 == 0) 0xFFE0E0E0 else 0xFFEEEEEE))) {
        Row(horizontalArrangement = Arrangement.SpaceBetween,
            modifier=Modifier.fillMaxWidth(1f)) {
            chatt.message?.let { Text(it, fontSize = 17.sp, modifier = Modifier.padding(4.dp, 10.dp, 4.dp, 10.dp)) }
            chatt.audio?.let {
                IconButton(onClick = {
                    audioPlayer.setupPlayer(it)
                    navController.navigate("AudioView?autoPlay=true")
                },
                    modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp).align(Alignment.Bottom)) {
                    Icon(painter = painterResource(android.R.drawable.stat_notify_voicemail),
                        contentDescription = stringResource(R.string.audio),
                        modifier = Modifier.scale(1.4f),
                        tint = Color.DarkGray
                    )
                }
            }
        }

        chatt.message?.let { Text(it, fontSize = 17.sp, modifier = Modifier.padding(4.dp, 10.dp, 4.dp, 10.dp)) }
    }
}