package edu.umich.yanfuguo.kotlinjpcchatter

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController

class PlayerUIState(): Parcelable {
    var recVisible = true
    var recEnabled = true
    var recColor = Color.Black
    var recIcon = R.drawable.ic_baseline_radio_button_checked_24 // initial value

    var playCtlEnabled = false
    var playCtlColor = Color.LightGray

    var stopIcon = R.drawable.ic_baseline_stop_24
    var ffwdIcon = R.drawable.ic_baseline_forward_10_24
    var rwndIcon = R.drawable.ic_baseline_replay_10_24

    var playEnabled = false
    var playColor = Color.LightGray
    var playIcon = R.drawable.ic_baseline_play_arrow_24 // initial value

    var doneEnabled = true
    var doneColor = Color.DarkGray
    var doneIcon = R.drawable.ic_baseline_share_24 // initial value

    constructor(parcel: Parcel) : this() {
        recVisible = parcel.readByte() != 0.toByte()
        recEnabled = parcel.readByte() != 0.toByte()
        recIcon = parcel.readInt()
        playCtlEnabled = parcel.readByte() != 0.toByte()
        playEnabled = parcel.readByte() != 0.toByte()
        playIcon = parcel.readInt()
        doneEnabled = parcel.readByte() != 0.toByte()
        doneIcon = parcel.readInt()
    }

    private fun playCtlEnabled(enabled: Boolean) {
        playCtlEnabled = enabled
        playCtlColor = if (enabled) Color.DarkGray else Color.LightGray
    }

    private fun playEnabled(enabled: Boolean) {
        playIcon = R.drawable.ic_baseline_play_arrow_24
        playEnabled = enabled
        playColor = if (enabled) Color.DarkGray else Color.LightGray
    }

    private fun pauseEnabled(enabled: Boolean) {
        playIcon = R.drawable.ic_baseline_pause_24
        playEnabled = enabled
        playColor = if (enabled) Color.DarkGray else Color.LightGray
    }

    private fun recEnabled() {
        recIcon = R.drawable.ic_baseline_radio_button_checked_24
        recEnabled = true
        recColor = Color.Black
    }

    private fun doneEnabled(enabled: Boolean) {
        doneEnabled = enabled
        doneColor = if (enabled) Color.DarkGray else Color.LightGray
    }

    fun propagate(playerState: PlayerState) = when (playerState) {
        is PlayerState.start -> {
            when (playerState.mode) {
                StartMode.play -> {
                    recVisible = false
                    recEnabled = false
                    recColor = Color.Transparent
                    playEnabled(true)
                    playCtlEnabled(false)
                    doneIcon = R.drawable.ic_baseline_exit_to_app_24
                    doneColor = Color.DarkGray
                }
                StartMode.standby -> {
                    if (recVisible) recEnabled()
                    playEnabled(true)
                    playCtlEnabled(false)
                    doneEnabled(true)
                }
                StartMode.record -> {
                    // initial values already set up for record start mode.
                }
            }
        }
        PlayerState.recording -> {
            recIcon = R.drawable.ic_outline_stop_circle_24
            recColor = Color.FilledMic
            playEnabled(false)
            playCtlEnabled(false)
            doneEnabled(false)
        }
        is PlayerState.paused -> {
            if (recVisible) recEnabled()
            playIcon = R.drawable.ic_baseline_play_arrow_24
        }
        is PlayerState.playing -> {
            if (recVisible) {
                recEnabled = false
                recColor = Color.LightGray
            }
            pauseEnabled(true)
            playCtlEnabled(true)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (recVisible) 1 else 0)
        parcel.writeByte(if (recEnabled) 1 else 0)
        parcel.writeInt(recIcon)
        parcel.writeByte(if (playCtlEnabled) 1 else 0)
        parcel.writeByte(if (playEnabled) 1 else 0)
        parcel.writeInt(playIcon)
        parcel.writeByte(if (doneEnabled) 1 else 0)
        parcel.writeInt(doneIcon)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlayerUIState> {
        override fun createFromParcel(parcel: Parcel): PlayerUIState {
            return PlayerUIState(parcel)
        }

        override fun newArray(size: Int): Array<PlayerUIState?> {
            return arrayOfNulls(size)
        }
    }
}

@Composable
fun AudioView(navController: NavHostController, audioPlayer: AudioPlayer, autoPlay: Boolean?) {
    val playerUIState by rememberSaveable { mutableStateOf(PlayerUIState()) }
    var isAutoPlay by rememberSaveable { mutableStateOf(autoPlay ?: false) }

    // Since AudioView uses AudioPlayer.playerState, it is automatically subscribed
    // to it. Everytime playerState is updated, AudioView will be recomposed. Upon
    // every (re)composition of AudioView, we call the propagate() method of
    // PlayerUIState, to update the modifiers of the UI elements according to the
    // current value of playerState.
    playerUIState.propagate(audioPlayer.playerState)
    if (isAutoPlay) {
        isAutoPlay = false  // ignore Android Studio warning that isAutoPlay is never used
        audioPlayer.playTapped()
    }
    Column(verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight(1f)) {
        Spacer(modifier=Modifier.fillMaxHeight(.1f))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier=Modifier.fillMaxWidth(1f)) {
            StopButton(audioPlayer, playerUIState)
            RwndButton(audioPlayer, playerUIState)
            PlayButton(audioPlayer, playerUIState)
            FfwdButton(audioPlayer, playerUIState)
            DoneButton(navController, audioPlayer, playerUIState)
        }
        RecButton(audioPlayer, playerUIState)
    }
}

@Composable
fun RecButton(audioPlayer: AudioPlayer, playerUIState: PlayerUIState) {
    Button(onClick = { audioPlayer.recTapped() },
        enabled = playerUIState.recEnabled,
        modifier = Modifier.fillMaxSize(.5f),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White,
            disabledBackgroundColor = Color.White),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Icon(painter = painterResource(playerUIState.recIcon),
            modifier=Modifier.scale(4f).weight(weight = .8f),
            contentDescription = stringResource(R.string.recButton),
            tint = playerUIState.recColor
        )
    }
}

@Composable
fun DoneButton(navController: NavController, audioPlayer: AudioPlayer, playerUIState: PlayerUIState) {
    Button(onClick = {
        audioPlayer.doneTapped()
        navController.popBackStack()
    },
        enabled = playerUIState.doneEnabled,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White,
            disabledBackgroundColor = Color.White),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Icon(painter = painterResource(playerUIState.doneIcon),
            modifier=Modifier.scale(1.7f).padding(end=8.dp),
            contentDescription = stringResource(R.string.doneButton),
            tint = playerUIState.doneColor
        )
    }
}

@Composable
fun StopButton(audioPlayer: AudioPlayer, playerUIState: PlayerUIState) {
    Button(onClick = {
        audioPlayer.stopTapped()
    },
        enabled = playerUIState.playCtlEnabled,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White,
            disabledBackgroundColor = Color.White),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Icon(painter = painterResource(playerUIState.stopIcon),
            modifier=Modifier.scale(1.7f).padding(end=8.dp),
            contentDescription = stringResource(R.string.stopButton),
            tint = playerUIState.playCtlColor
        )
    }
}

@Composable
fun RwndButton(audioPlayer: AudioPlayer, playerUIState: PlayerUIState) {
    Button(onClick = {
        audioPlayer.rwndTapped()
    },
        enabled = playerUIState.playCtlEnabled,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White,
            disabledBackgroundColor = Color.White),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Icon(painter = painterResource(playerUIState.rwndIcon),
            modifier=Modifier.scale(1.7f).padding(end=8.dp),
            contentDescription = stringResource(R.string.rwndButton),
            tint = playerUIState.playCtlColor
        )
    }
}

@Composable
fun PlayButton(audioPlayer: AudioPlayer, playerUIState: PlayerUIState) {
    Button(onClick = {
        audioPlayer.playTapped()
    },
        enabled = playerUIState.playEnabled,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White,
            disabledBackgroundColor = Color.White),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Icon(painter = painterResource(playerUIState.playIcon),
            modifier=Modifier.scale(1.7f).padding(end=8.dp),
            contentDescription = stringResource(R.string.playButton),
            tint = playerUIState.playColor
        )
    }
}

@Composable
fun FfwdButton(audioPlayer: AudioPlayer, playerUIState: PlayerUIState) {
    Button(onClick = {
        audioPlayer.ffwdTapped()
    },
        enabled = playerUIState.playCtlEnabled,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White,
            disabledBackgroundColor = Color.White),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Icon(painter = painterResource(playerUIState.ffwdIcon),
            modifier=Modifier.scale(1.7f).padding(end=8.dp),
            contentDescription = stringResource(R.string.ffwdButton),
            tint = playerUIState.playCtlColor
        )
    }
}