package com.chesire.malime.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.chesire.malime.util.sec.Decryptor
import com.chesire.malime.util.sec.Encryptor

private const val authAlias: String = "private_auth"
private const val preferenceAuth: String = "auth"
private const val preferenceUsername: String = "username"
private const val preferenceAllowCrashReporting: String = "allowCrashReporting"
private const val preferenceAnimeFilterLength: String = "animeFilterLength"
private const val preferenceAutoUpdateState: String = "autoUpdateState"

class SharedPref(
    context: Context
) {
    val preferenceAnimeFilter: String = "animeFilter"
    val preferenceAnimeSortOption: String = "animeSortOption"
    val sharedPrefFile: String = "malime_shared_pref"

    private val sharedPreferences =
        context.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
    private val encryptor = Encryptor(context.applicationContext)
    private val decryptor = Decryptor()

    fun getAuth(service: SupportedService): String {
        val text = sharedPreferences.getString("${service.name} $preferenceAuth", "")

        return if (text.isNotBlank()) {
            decryptor.decryptData(
                authAlias,
                Base64.decode(text, Base64.DEFAULT)
            )
        } else {
            ""
        }
    }

    fun putAuth(auth: String, service: SupportedService): SharedPref {
        val encrypted = encryptor.encryptText(authAlias, auth)

        sharedPreferences.edit()
            .putString(
                "${service.name} $preferenceAuth",
                Base64.encodeToString(encrypted, Base64.DEFAULT)
            )
            .apply()

        return this
    }

    fun getUsername(service: SupportedService): String {
        return sharedPreferences.getString("${service.name} $preferenceUsername", "")
    }

    fun putUsername(username: String, service: SupportedService): SharedPref {
        sharedPreferences.edit()
            .putString("${service.name} $preferenceUsername", username)
            .apply()

        return this
    }

    fun getAllowCrashReporting(): Boolean {
        return sharedPreferences.getBoolean(preferenceAllowCrashReporting, true)
    }

    fun getAnimeFilter(): BooleanArray {
        val filterLength = sharedPreferences.getInt(preferenceAnimeFilterLength, 0)
        if (filterLength == 0) {
            return getDefaultFilter()
        }

        val returnArray = BooleanArray(filterLength)
        for (i in 0 until filterLength) {
            returnArray[i] = sharedPreferences.getBoolean(preferenceAnimeFilter + i, false)
        }

        return returnArray
    }

    fun setAnimeFilter(input: BooleanArray): SharedPref {
        val editor = sharedPreferences.edit()
        editor.putInt(preferenceAnimeFilterLength, input.count())
        for (i in input.indices) {
            editor.putBoolean(preferenceAnimeFilter + i, input[i])
        }
        editor.apply()

        return this
    }

    fun getAnimeSortOption(): Int {
        // If doesn't exist, return "Title"
        return sharedPreferences.getInt(preferenceAnimeSortOption, 1)
    }

    fun setAnimeSortOption(sortOption: Int): SharedPref {
        sharedPreferences.edit()
            .putInt(preferenceAnimeSortOption, sortOption)
            .apply()

        return this
    }

    fun getAutoUpdateSeriesState(): Boolean {
        return sharedPreferences.getBoolean(preferenceAutoUpdateState, false)
    }

    fun clearLoginDetails() {
        sharedPreferences.edit()
            .remove(preferenceAuth)
            .remove(preferenceUsername)
            .apply()
    }

    fun registerOnChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun getDefaultFilter(): BooleanArray {
        return booleanArrayOf(true, false, false, false, false)
    }
}