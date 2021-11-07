package edu.umich.yanfuguo.kotlinjpcchatter

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.*
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.*
import java.io.File
import java.time.Instant
import javax.crypto.Cipher
import kotlin.text.Charsets.ISO_8859_1

object ChatterID {
    var expiration = Instant.EPOCH


    // ChatterID.id is null when either the user hasn’t obtained
    // a chatterID from the back end or the ID has expired.
    var id: String? = null
        get() {
            return if (Instant.now() >= expiration) null else field
        }
        set(newValue) {
            field = newValue
        }
}