package com.chesire.nekome.app.login.anilist

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import timber.log.Timber

private const val ACCESS_TOKEN_REGEX = "(?:access_token=)(.*?)(?:&)"

@HiltViewModel
class AniListLoginViewModel @Inject constructor() : ViewModel() {

    /**
     * Retrieves the access token from the passed in [uri], or null if failure.
     */
    fun retrieveAccessToken(uri: Uri?): String? {
        if (uri == null) {
            Timber.e("AniListLogin passed in Uri is null")
            return null
        }

        return ACCESS_TOKEN_REGEX.toRegex()
            .find(uri.fragment.toString())
            ?.groups
            ?.get(1)
            ?.value
    }
}
