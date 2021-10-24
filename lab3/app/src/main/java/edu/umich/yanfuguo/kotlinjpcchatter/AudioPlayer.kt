package edu.umich.yanfuguo.kotlinjpcchatter

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AudioPlayer() {
    var playerState by mutableStateOf<PlayerState>(PlayerState.start(StartMode.standby))
    var audio = ByteArray(0)

    private lateinit var audioFilePath: String
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaRecorder: MediaRecorder

    constructor(context: Context, extFilePath: String): this() {
        audioFilePath = extFilePath
        mediaPlayer = MediaPlayer()
        // API Level 31: MediaRecorder needs context
        mediaRecorder = MediaRecorder(/*context*/)
        // Otherwise ignore Android Studio's warning that context is never used
    }
}