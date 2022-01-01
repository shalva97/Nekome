package com.chesire.nekome.app.login.kitsu

/**
 * Provides the status of a login request.
 */
enum class LoginStatus {
    EmptyUsername,
    EmptyPassword,
    Error,
    InvalidCredentials,
    Loading,
    Success
}
