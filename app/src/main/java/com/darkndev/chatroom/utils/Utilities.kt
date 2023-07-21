package com.darkndev.chatroom.utils

import android.util.Log
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

inline fun <T> httpResponse(
    request: () -> HttpResponse,
    success: (HttpResponse) -> T,
    error: (HttpResponse?, HttpStatusCode?, Throwable) -> T
) = try {
    success(request())
} catch (e: RedirectResponseException) {
    //3xx - responses
    Log.e("AuthApiImpl", "RedirectResponseException: ", e)
    error(e.response, e.response.status, e)
} catch (e: ClientRequestException) {
    //4xx - responses
    Log.e("AuthApiImpl", "ClientRequestException: ", e)
    error(e.response, e.response.status, e)
} catch (e: ServerResponseException) {
    //5xx - responses
    Log.e("AuthApiImpl", "ServerResponseException: ", e)
    error(e.response, e.response.status, e)
} catch (e: Exception) {
    //others - responses
    Log.e("AuthApiImpl", "Exception: ", e)
    error(null, null, e)
}

inline fun <T> socketResponse(
    request: () -> ClientWebSocketSession,
    socket: (ClientWebSocketSession) -> T,
    error: (Throwable) -> T
) = try {
    socket(request())
} catch (e: Exception) {
    error(e)
}

fun getFormatTime(modifiedMillis: Long): String {
    val modified =
        ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(modifiedMillis),
            ZoneId.systemDefault()
        )
    val now = ZonedDateTime.now()

    return if (modified.year != now.year) {
        modified.year.toString()
    } else if (modified.dayOfYear != now.dayOfYear) {
        DateTimeFormatter
            .ofPattern("MMM d", Locale.US)
            .format(modified)
    } else {
        DateTimeFormatter
            .ofPattern("h:mm a", Locale.US)
            .format(modified)
    }
}