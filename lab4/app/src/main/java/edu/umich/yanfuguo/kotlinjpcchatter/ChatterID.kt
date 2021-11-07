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
    private val keyStore = ChatterKeyStore(authenticate = true)

    private lateinit var bioAuthPrompt: Class3BiometricOrCredentialAuthPrompt
    private lateinit var authPrompt: AuthPrompt

    private val authPromptExCatcher = CoroutineExceptionHandler { _, error ->
        authPrompt.cancelAuthentication()
        Log.e("AuthPrompt exception", error.localizedMessage ?: "authentication cancelled")
    }

    @ExperimentalCoroutinesApi
    private suspend fun Class3BiometricOrCredentialAuthPrompt.authenticate(host: AuthPromptHost, crypto: BiometricPrompt.CryptoObject): BiometricPrompt.AuthenticationResult? =
        suspendCancellableCoroutine { cont ->
            authPrompt = startAuthentication(host, crypto, object : AuthPromptCallback() {
                override fun onAuthenticationSucceeded(
                    activity: FragmentActivity?,
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(activity,result)
                    cont.resume(result, null)
                }

                override fun onAuthenticationError(activity: FragmentActivity?, @BiometricPrompt.AuthenticationError errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(activity, errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                        cont.resume(null, null)
                    }
                }
                // default to super for onAuthenticationFailed()
            })
            cont.invokeOnCancellation { authPrompt.cancelAuthentication() }
        }


    // run only once upon app launch
    @ExperimentalCoroutinesApi
    suspend fun open(context: Context) {
        if (expiration != Instant.EPOCH) { // this is not first launch
            return
        }

        val status = BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG)
        if (status != BiometricManager.BIOMETRIC_SUCCESS) {
            context.toast("Skipping biometric authentication ($status)")
            return
        }

        bioAuthPrompt = Class3BiometricOrCredentialAuthPrompt.Builder("Biometric ID").apply {
            setSubtitle("for kotlinChatter")
            setDescription("To manage persistent ChatterID")
            setConfirmationRequired(false)
        }.build()

        context.getSharedPreferences(ID_FILE, Context.MODE_PRIVATE)
            .getString(KEY_NAME, null)?.let {
                val idExp = Instant.parse(it.takeLast(INSTANT_LENGTH))

                val idVal = it.dropLast(INSTANT_LENGTH).toByteArray(ISO_8859_1)
                val iv = idVal.takeLast(IV_LENGTH).toByteArray()
                val idEnc = idVal.dropLast(IV_LENGTH).toByteArray()
                val decryptor = keyStore.createCipher(KEY_NAME, iv)

                val cryptoObject = BiometricPrompt.CryptoObject(decryptor)
                withContext(authPromptExCatcher) {
                    val authResult = bioAuthPrompt.authenticate(
                        AuthPromptHost(context as FragmentActivity),
                        cryptoObject
                    )
                    authResult?.cryptoObject?.cipher?.run {
                        id = String(doFinal(idEnc))
                        expiration = idExp
                    } ?: run {
                        context.toast("KeyStore not read")
                    }

                }
            }
    }

    // save() could run multiple times during the lifetime of the app,
    // once every chatterID lifetime. Since we only need one instance
    // of the encryptor cipher, we create it using the lazy() delegate.
    @ExperimentalCoroutinesApi
    suspend fun save(context: Context) {
        val status = BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG)
        if (status != BiometricManager.BIOMETRIC_SUCCESS) {
            context.toast("Skipping biometric authentication ($status)")
            return
        }

        id?.let {
            val encryptor: Cipher by lazy { keyStore.createCipher(KEY_NAME) }

            val cryptoObject = BiometricPrompt.CryptoObject(encryptor)
            withContext(authPromptExCatcher) {
                val authResult = bioAuthPrompt.authenticate(
                    AuthPromptHost(context as FragmentActivity),
                    cryptoObject
                )
                authResult?.cryptoObject?.cipher?.run {
                    val idEnc = doFinal(it.toByteArray(ISO_8859_1))
                    val idVal = String(idEnc + iv, ISO_8859_1) + expiration.toString()

                    context.getSharedPreferences(ID_FILE, Context.MODE_PRIVATE)
                        .edit().putString(KEY_NAME, idVal).apply()
                } ?: run {
                    context.toast("KeyStore not updated")
                }
            }
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