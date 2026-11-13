package com.notnex.myday.data

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Шифрует секреты вроде ключа нейросети перед сохранением в DataStore.
 *
 * Главный ключ создаётся в Android Keystore и никогда его не покидает: прочитать его нельзя
 * даже с root, и в резервную копию он не попадает. Поэтому зашифрованная строка бесполезна
 * вне этого телефона. На новом телефоне [decrypt] вернёт null, и человек введёт ключ заново.
 *
 * Первое обращение к Keystore занимает десятки миллисекунд — вызывать не из главного потока.
 */
@Singleton
class SecretStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val aead: Aead by lazy { createAead() }

    /** Возвращает зашифрованный [plain] строкой, пригодной для stringPreferencesKey. */
    fun encrypt(plain: String): String {
        val cipher = aead.encrypt(plain.toByteArray(), ASSOCIATED_DATA)
        return Base64.encodeToString(cipher, Base64.NO_WRAP)
    }

    /** Расшифровывает строку из [encrypt]; null — расшифровать нельзя, например после переезда на новый телефон. */
    fun decrypt(encrypted: String): String? = try {
        val cipher = Base64.decode(encrypted, Base64.NO_WRAP)
        String(aead.decrypt(cipher, ASSOCIATED_DATA))
    } catch (e: GeneralSecurityException) {
        Log.w(TAG, "Secret can't be decrypted", e)
        null
    } catch (e: IllegalArgumentException) {
        // Строка повреждена и не читается как Base64.
        Log.w(TAG, "Secret is not valid Base64", e)
        null
    }

    private fun createAead(): Aead {
        AeadConfig.register()
        val keysetHandle = try {
            buildKeysetManager().keysetHandle
        } catch (e: Exception) {
            // Набор ключей зашифрован главным ключом, которого на этом телефоне нет
            // (например, файл вернулся из резервной копии). Старые секреты всё равно
            // не расшифровать, поэтому начинаем с чистого набора.
            Log.w(TAG, "Keyset is unreadable, creating a new one", e)
            context.deleteSharedPreferences(KEYSET_PREFS)
            buildKeysetManager().keysetHandle
        }
        return keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    // Набор рабочих ключей Tink лежит в SharedPreferences зашифрованным главным ключом из Keystore.
    private fun buildKeysetManager(): AndroidKeysetManager = AndroidKeysetManager.Builder()
        .withSharedPref(context, KEYSET_NAME, KEYSET_PREFS)
        .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
        .withMasterKeyUri(MASTER_KEY_URI)
        .build()

    private companion object {
        const val TAG = "SecretStore"
        const val KEYSET_NAME = "secret_keyset"
        // Имя файла исключено из резервной копии в backup_rules.xml и data_extraction_rules.xml.
        const val KEYSET_PREFS = "secret_keyset_prefs"
        const val MASTER_KEY_URI = "android-keystore://myday_secret_master_key"
        // Привязывает шифротекст к назначению: строку из этого хранилища не подсунуть в другое место.
        val ASSOCIATED_DATA = "myday-secret".toByteArray()
    }
}
