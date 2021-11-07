package edu.umich.yanfuguo.kotlinjpcchatter

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.Cipher.DECRYPT_MODE
import javax.crypto.Cipher.ENCRYPT_MODE
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEY_STORE = "AndroidKeyStore"
private const val KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
private const val KEY_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
private const val KEY_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE // GCM requires no padding

class ChatterKeyStore(val authenticate: Boolean) {

    private fun getKey(keyName: String): SecretKey {
        return KeyStore.getInstance(KEY_STORE)
            .apply {
                load(null)
            }.getKey(keyName, null) as? SecretKey
            ?: KeyGenerator.getInstance(KEY_ALGORITHM, KEY_STORE)
                .apply {
                    init(
                        KeyGenParameterSpec.Builder(keyName,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KEY_BLOCK_MODE)
                            .setEncryptionPaddings(KEY_PADDING)
                            .setKeySize(256)
                            .setUserAuthenticationRequired(authenticate)
                            .build()
                    )
                }.generateKey()
    }

    fun createCipher(keyName: String, iv: ByteArray? = null): Cipher {
        return Cipher.getInstance("$KEY_ALGORITHM/$KEY_BLOCK_MODE/$KEY_PADDING")
            .apply {
                iv?.let {
                    init(DECRYPT_MODE, getKey(keyName), GCMParameterSpec(128, it))
                } ?: run {
                    init(ENCRYPT_MODE, getKey(keyName))
                }
            }
    }
}