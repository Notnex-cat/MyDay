package com.notnex.myday.neuralnetwork

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.notnex.myday.R

/**
 * Понятный человеку текст ошибки нейросети по HTTP-коду ответа.
 * null — ответа не было вовсе: нет интернета, VPN или сервер не отвечает.
 * У [R.string.ai_error_unknown] есть место под код: stringResource(id, code).
 */
@StringRes
fun aiErrorMessage(httpCode: Int?): Int = when (httpCode) {
    null -> R.string.ai_error_network
    // У Gemini 400 приходит и тогда, когда сервис недоступен в стране пользователя.
    400 -> R.string.ai_error_bad_request
    401 -> R.string.ai_error_unauthorized
    // DeepSeek и Mistral так отвечают, когда на счёте кончились деньги.
    402 -> R.string.ai_error_payment
    403 -> R.string.ai_error_forbidden
    404 -> R.string.ai_error_not_found
    408 -> R.string.ai_error_timeout
    429 -> R.string.ai_error_rate_limit
    500 -> R.string.ai_error_server
    502, 503, 504 -> R.string.ai_error_unavailable
    else -> R.string.ai_error_unknown
}

@Composable
fun aiErrorText(error: Throwable): String = when (error) {
    is AiKeyMissingException -> stringResource(R.string.no_api_key)
    is AiResponseFormatException -> stringResource(R.string.ai_error_bad_response)
    is AiHttpException -> {
        val text = stringResource(aiErrorMessage(error.code), error.code)
        "$text (HTTP ${error.code})"
    }
    // Ответа не было вовсе: нет сети, VPN, таймаут.
    else -> stringResource(aiErrorMessage(null))
}
