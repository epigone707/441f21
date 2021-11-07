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

    private const val ID_FILE = "Chatter"
    private const val KEY_NAME = "ChatterID"
    private const val INSTANT_LENGTH = 24
    private const val IV_LENGTH = 12

    // false if encryption without authentication
    private val keyStore = ChatterKeyStore(authenticate = false)


    // run only once upon app launch
    suspend fun open(context: Context) {
        if (expiration != Instant.EPOCH) { // this is not first launch
            return
        }

        context.getSharedPreferences(ID_FILE, Context.MODE_PRIVATE)
            .getString(KEY_NAME, null)?.let {
                expiration = Instant.parse(it.takeLast(INSTANT_LENGTH))
                // id = it.dropLast(INSTANT_LENGTH)
                val idVal = it.dropLast(INSTANT_LENGTH).toByteArray(ISO_8859_1)
                val iv = idVal.takeLast(IV_LENGTH).toByteArray()
                val idEnc = idVal.dropLast(IV_LENGTH).toByteArray()
                val decryptor = keyStore.createCipher(KEY_NAME, iv)
                id = String(decryptor.doFinal(idEnc))
            }
    }

    // save() could run multiple times during the lifetime of the app,
    // once every chatterID lifetime. Since we only need one instance
    // of the encryptor cipher, we create it using the lazy() delegate.
    suspend fun save(context: Context) {
        id?.let {
            // val idVal = id+expiration.toString()

            // The lazy() delegate defers initialization of a variable until
            // its first use and, like singleton, there will only be one instance
            // of it and its creation is thread-safe.
            val encryptor: Cipher by lazy { keyStore.createCipher(KEY_NAME) }
            val idEnc = encryptor.doFinal(it.toByteArray(ISO_8859_1))
            val idVal = String(idEnc + encryptor.iv, ISO_8859_1)+expiration.toString()
            context.getSharedPreferences(ID_FILE, Context.MODE_PRIVATE)
                .edit().putString(KEY_NAME, idVal).apply()
        }
    }

    // test func
    fun delete(context: Context) {
        val folder = File(context.getFilesDir().getParent()?.toString() + "/shared_prefs/")
        val files = folder.list()
        files?.forEach {
            context.getSharedPreferences(it.replace(".xml", ""), Context.MODE_PRIVATE)
                .edit().clear().apply() // clear each preference file from memory
            File(folder, it).delete()   // delete the file
        }
    }
}