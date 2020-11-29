package com.chesire.nekome.kitsu.auth

import com.chesire.nekome.auth.api.AuthEntity
import com.chesire.nekome.kitsu.auth.dto.AuthResponseDto
import javax.inject.Inject

// TODO #387
/**
 * Provides ability to map instances of [AuthResponseDto] into [AuthEntity].
 */
class KitsuAuthDtoMapper @Inject constructor() {
    fun from(input: AuthResponseDto) = AuthEntity()
}
