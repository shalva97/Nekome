package com.chesire.nekome.app.login

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import timber.log.Timber

class AniListLoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data: Uri? = intent.data
        val intent = Intent(this, Class.forName("com.chesire.nekome.Activity"))

        val regex = "(?:access_token=)(.*?)(?:&)".toRegex()
        val matchResult = regex.find(data?.fragment.toString())
        if (matchResult?.groups?.get(1) != null) {
            val token = matchResult.groups[1]!!.value
            Timber.i("Successfully logged into AniList")

            // TODO: Save token
            intent.putExtra("Success", true)
        } else {
            intent.putExtra("Success", false)
        }
        finish()

        // Inject the "Class" here? Or make something we can inject that handles redirecting to activities
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}
