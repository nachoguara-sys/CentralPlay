package com.centralplay.app.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

/** Allows switching between authorized backend hosts without rebuilding Retrofit. */
class DynamicHostInterceptor(initialHost: String? = null) : Interceptor {
    private val activeHost = AtomicReference(initialHost?.trim()?.takeIf { it.isNotBlank() })

    fun updateHost(newHost: String?) {
        activeHost.set(newHost?.trim()?.takeIf { it.isNotBlank() })
    }

    fun currentHost(): String? = activeHost.get()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val host = activeHost.get()
        if (host.isNullOrBlank()) return chain.proceed(original)
        val updatedUrl = original.url.newBuilder().host(host).build()
        return chain.proceed(original.newBuilder().url(updatedUrl).build())
    }
}
