package edu.umich.yanfuguo.kotlinjpcchatter

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.graphics.Color

fun Context.toast(message: String, short: Boolean = true) {
    Toast.makeText(this, message, if (short) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
}

val Color.Companion.OpenMic: Color
    get() = Color(0xFF8A9A5B)

val Color.Companion.FilledMic: Color
    get() = Color(0xFFB22222)