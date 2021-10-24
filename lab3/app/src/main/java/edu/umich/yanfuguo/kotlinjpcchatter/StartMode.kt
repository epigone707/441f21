package edu.umich.yanfuguo.kotlinjpcchatter

enum class StartMode {
    standby, record, play
}

enum class TransEvent {
    recTapped, playTapped, stopTapped, failed
}
sealed class PlayerState {
    class start(val mode: StartMode): PlayerState()
    object recording: PlayerState()
    class playing(val parent: StartMode): PlayerState()
    class paused(val grand: StartMode): PlayerState()
    fun transition(event: TransEvent): PlayerState {
        return when (this) {
            is start -> when (mode) {
                StartMode.record -> if (event == TransEvent.recTapped) recording else this
                StartMode.play -> if (event == TransEvent.playTapped) playing(StartMode.play) else this
                StartMode.standby -> when (event) {
                    TransEvent.recTapped -> recording
                    TransEvent.playTapped -> playing(StartMode.standby)
                    else -> this
                }
            }
            recording -> when (event) {
                TransEvent.recTapped -> start(StartMode.standby)
                TransEvent.failed -> start(StartMode.record)
                else -> this
            }
            is playing -> when (event) {
                TransEvent.playTapped -> paused(this.parent)
                TransEvent.stopTapped, TransEvent.failed -> start(this.parent)
                else -> this
            }
            is paused -> when (event) {
                TransEvent.recTapped -> recording
                TransEvent.playTapped -> playing(this.grand)
                TransEvent.stopTapped -> start(StartMode.standby)
                else -> this
            }
        }
    }
}