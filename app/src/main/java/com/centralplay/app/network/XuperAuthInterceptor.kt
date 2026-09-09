package com.centralplay.app.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Compatibility interceptor for the imported reforma.
 * It only uses hosts and headers explicitly configured for Central Play.
 * No third-party credentials or private endpoints are embedded here.
 */
class XuperAuthInterceptor(
    private val catalogHosts: List<String> = emptyList(),
    private val configuredHeaders: Map<String, String> = emptyMap()
) : Interceptor {
    private var currentHostIndex = 0

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
        configuredHeaders.forEach { (key, value) -> if (key.isNotBlank()) builder.header(key, value) }

        if (catalogHosts.isNotEmpty()) {
            val host = catalogHosts[currentHostIndex % catalogHosts.size]
            if (host.isNotBlank()) builder.url(original.url.newBuilder().host(host).build())
        }

        val response = chain.proceed(builder.build())
        if (!response.isSuccessful && catalogHosts.isNotEmpty()) {
            currentHostIndex = (currentHostIndex + 1) % catalogHosts.size
        }
        return response
    }
}
