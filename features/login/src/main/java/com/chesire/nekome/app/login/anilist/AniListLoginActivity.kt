package com.chesire.nekome.app.login.anilist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import timber.log.Timber

class AniListLoginActivity : ComponentActivity() {

    private val viewModel: AniListLoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data: Uri? = intent.data
        val accessToken = viewModel.retrieveAccessToken(data)
        if (accessToken == null) {
            Timber.w("Failed to log into AniList")
            intent.putExtra("Success", false)
        } else {
            Timber.i("Successfully logged into AniList")
            intent.putExtra("Success", true)
            // TODO: Save token
        }

        finish()
        val intent = Intent(this, Class.forName("com.chesire.nekome.Activity"))

        // Inject the "Class" here? Or make something we can inject that handles redirecting to activities
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}
