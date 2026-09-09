package com.centralplay.app.network

import com.centralplay.app.model.NetworkConfig
import okhttp3.Request
import java.io.IOException

class FailoverApiClient(private val config: NetworkConfig) {
    private val client = NetworkClientFactory.okHttp(config)
    private val service = NetworkClientFactory.retrofit(client).create(CentralApiService::class.java)
    private val pool = EndpointPool(config.apiNodes, config.nodeCooldownSeconds.coerceAtLeast(1) * 1000L)

    fun hasNodes(): Boolean = !pool.isEmpty()

    @Throws(IOException::class)
    fun getText(path: String, query: Map<String,String> = emptyMap(), headers: Map<String,String> = emptyMap()): String {
        if(pool.isEmpty()) throw IOException("No API nodes configured")
        var last: Throwable? = null
        repeat(pool.size()) {
            val node=pool.current() ?: throw IOException("No API node available")
            val url=node.trimEnd('/') + if(path.startsWith('/')) path else "/$path"
            try {
                val response=service.getText(url,headers,query).execute()
                if(!response.isSuccessful) throw IOException("HTTP ${response.code()} at ${hostLabel(url)}")
                val body=response.body() ?: throw IOException("Empty response from ${hostLabel(url)}")
                pool.markHealthy(node); return body
            } catch(t:Throwable){ last=t; pool.markFailed(node); pool.rotate() }
        }
        throw IOException("All API nodes failed",last)
    }

    fun healthCheck(): Boolean {
        if(pool.isEmpty()) return false
        for(node in pool.all()) {
            val url=node.trimEnd('/')+if(config.healthPath.startsWith('/'))config.healthPath else "/${config.healthPath}"
            try {
                client.newCall(Request.Builder().url(url).head().build()).execute().use { response ->
                    if(response.isSuccessful || response.code in 300..499){ pool.markHealthy(node); return true }
                }
            } catch(_:Throwable){ pool.markFailed(node) }
        }
        return false
    }
    private fun hostLabel(url:String):String=try{java.net.URI(url).host?:url}catch(_:Exception){url}
}
