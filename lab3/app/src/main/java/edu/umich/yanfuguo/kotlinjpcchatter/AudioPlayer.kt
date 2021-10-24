package edu.umich.yanfuguo.kotlinjpcchatter

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.*

class AudioPlayer() : Parcelable {
    // Upon instantiation, AudioPlayer starts in the standby mode of its start state
    var playerState by mutableStateOf<PlayerState>(PlayerState.start(StartMode.standby))
    var audio = ByteArray(0)

    private lateinit var audioFilePath: String
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaRecorder: MediaRecorder
    

    constructor(parcel: Parcel) : this() {
        audio = parcel.createByteArray() ?: ByteArray(0)
        audioFilePath = parcel.readString()!!
    }

    constructor(context: Context, extFilePath: String): this() {
        audioFilePath = extFilePath
        mediaPlayer = MediaPlayer()
        // API Level 31: MediaRecorder needs context
        mediaRecorder = MediaRecorder(/*context*/)
        // Otherwise ignore Android Studio's warning that context is never used
    }
    fun setupRecorder() {
        playerState = PlayerState.start(StartMode.record)
        audio = ByteArray(0)
    }

    fun setupPlayer(audioStr: String) {
        playerState = PlayerState.start(StartMode.play)
        audio = Base64.decode(audioStr, Base64.DEFAULT)
        preparePlayer()
    }

    /**
     * prepare the media player
     */
    private fun preparePlayer() {
        // a listener for when the player finishes playing.
        // In this case, we call the transition() method of PlayerState
        // to transit playerState to the appropriate state (as shown
        // in Figure 1) as if the stop button has been pressed.
        mediaPlayer.setOnCompletionListener {
            playerState = playerState.transition(TransEvent.stopTapped)
        }

        val os: OutputStream = try { FileOutputStream(audioFilePath) } catch (e: IOException) {
            Log.e("preparePlayer: ", e.localizedMessage ?: "IOException")
            return
        }
        os.write(audio)
        os.close()

        with (mediaPlayer) {
            setDataSource(audioFilePath)
            setVolume(1.0f, 1.0f) // 0.0 to 1.0 raw scalar
            prepare()
        }
    }

    fun playTapped() {
        playerState = playerState.transition(TransEvent.playTapped)
        with (mediaPlayer) {
            if (isPlaying) {
                pause()
            } else {
                this.start()
            }
        }
    }

    fun ffwdTapped() {
        mediaPlayer.seekTo(mediaPlayer.currentPosition+10000)
    }

    fun rwndTapped() {
        mediaPlayer.seekTo(mediaPlayer.currentPosition-15000)
    }


    /**
     * If the stop button is tapped, we also only pause playback instead
     * of stopping it. According to the MediaPlayer state diagram, once
     * the MediaPlayer is stopped, we can’t restart play back without
     * preparing the player again (which could throw an IO exception that
     * needs to be caught). So instead, we simply reset the play head to
     * the beginning of the audio clip and call transition() to transition
     * playerState appropriately
     */
    fun stopTapped() {
        mediaPlayer.pause()
        mediaPlayer.seekTo(0)
        playerState = playerState.transition(TransEvent.stopTapped)
    }

    fun recTapped() {
        if (playerState == PlayerState.recording) {
            finishRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        // reset player because we'll be re-using the output file that
        // may have been primed at the player.
        mediaPlayer.reset()

        playerState = playerState.transition(TransEvent.recTapped)

        with (mediaRecorder) {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFilePath)
            try {
                // call the prepare() method of MediaRecorder
                prepare()
            } catch (e: IOException) {
                Log.e("startRecording: ", e.localizedMessage ?: "IOException")
                return
            }
            // start recording by calling the start() method of MediaRecorder
            this.start()
        }
    }

    private fun finishRecording() {
        mediaRecorder.stop()
        mediaRecorder.reset()

        // load the recorded clip into the audio property
        // (to be uploaded to the Chatter back end along with the posted chatt)
        try {
            var read: Int
            audio = ByteArray(65536)
            val fis = FileInputStream(audioFilePath)
            val bos = ByteArrayOutputStream()
            while (fis.read(audio, 0, audio.size).also { read = it } != -1) {
                bos.write(audio, 0, read)
            }
            audio = bos.toByteArray()
            bos.close()
            fis.close()
        } catch (e: IOException) {
            Log.e("finishRecording: ", e.localizedMessage ?: "IOException")
            playerState = playerState.transition(TransEvent.failed)
            return
        }
        playerState = playerState.transition(TransEvent.recTapped)

        // prepare the MediaPlayer in case the user wants to play
        // back the recorded audio before posting it.
        preparePlayer()
    }

    /**
     * Once the user is satisfied with the recording, they tap the done button,
     * which calls the doneTapped() method to reset both the MediaPlayer and MediaRecorder.
     */
    fun doneTapped() {
        mediaPlayer.reset()
        mediaRecorder.reset()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByteArray(audio)
        parcel.writeString(audioFilePath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudioPlayer> {
        override fun createFromParcel(parcel: Parcel): AudioPlayer {
            return AudioPlayer(parcel)
        }

        override fun newArray(size: Int): Array<AudioPlayer?> {
            return arrayOfNulls(size)
        }
    }
}