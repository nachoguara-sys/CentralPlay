package com.centralplay.app.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

/**
 * Injects only headers explicitly supplied by an authorized Central Play backend/configuration.
 * Transport-managed headers such as Host, Connection and Accept-Encoding are intentionally
 * left to OkHttp.
 */
class AuthorizedHeadersInterceptor(
    initialHeaders: Map<String, String> = emptyMap()
) : Interceptor {
    private val headersRef = AtomicReference(initialHeaders.clean())

    fun updateHeaders(headers: Map<String, String>) {
        headersRef.set(headers.clean())
    }

    fun currentHeaders(): Map<String, String> = headersRef.get().toMap()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        headersRef.get().forEach { (name, value) -> builder.header(name, value) }
        return chain.proceed(builder.build())
    }

    private companion object {
        private val forbiddenTransportHeaders = setOf(
            "host", "connection", "accept-encoding", "content-length", "transfer-encoding"
        )

        fun Map<String, String>.clean(): Map<String, String> =
            entries.asSequence()
                .filter { it.key.isNotBlank() && it.value.isNotBlank() }
                .filter { it.key.lowercase() !in forbiddenTransportHeaders }
                .associate { it.key.trim() to it.value.trim() }
    }
}
