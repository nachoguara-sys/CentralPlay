package com.centralplay.app.catalog

import com.centralplay.app.network.EndpointPool
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.io.IOException

class ChannelCatalogRepository(
    private val client: OkHttpClient,
    apiNodes: List<String>,
    cooldownMillis: Long = 30_000L
) {
    private val pool = EndpointPool(apiNodes, cooldownMillis)
    private val service: CatalogService = Retrofit.Builder()
        .baseUrl("https://localhost/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CatalogService::class.java)

    suspend fun loadChannels(
        userId: String,
        path: String = "/api/layout/channels"
    ): Result<List<Channel>> {
        if (pool.isEmpty()) return Result.failure(IOException("No catalog nodes configured"))
        var lastError: Throwable? = null

        repeat(pool.size()) {
            val node = pool.current() ?: return@repeat
            val url = node.trimEnd('/') + if (path.startsWith('/')) path else "/$path"
            try {
                val body = mapOf(
                    "user_id" to userId,
                    "api_version" to "v2",
                    "lang" to "es"
                )
                val response = service.getChannelsCatalog(url, body)
                if (response.isSuccessful) {
                    val channels = response.body()?.toChannels().orEmpty()
                    if (channels.isNotEmpty()) {
                        pool.markHealthy(node)
                        return Result.success(channels)
                    }
                    lastError = IOException("Catalog returned no channels")
                } else {
                    lastError = IOException("HTTP ${response.code()}")
                }
            } catch (t: Throwable) {
                lastError = t
            }
            pool.markFailed(node)
            pool.rotate()
        }
        return Result.failure(lastError ?: IOException("All catalog nodes failed"))
    }
}
