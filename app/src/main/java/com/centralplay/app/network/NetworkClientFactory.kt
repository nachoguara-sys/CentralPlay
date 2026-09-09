package com.centralplay.app.network

import com.centralplay.app.model.NetworkConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClientFactory {
    fun okHttp(config: NetworkConfig): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.connectTimeoutSeconds.coerceAtLeast(1).toLong(),TimeUnit.SECONDS)
        .readTimeout(config.readTimeoutSeconds.coerceAtLeast(1).toLong(),TimeUnit.SECONDS)
        .writeTimeout(config.readTimeoutSeconds.coerceAtLeast(1).toLong(),TimeUnit.SECONDS)
        .pingInterval(config.pingIntervalSeconds.coerceAtLeast(1).toLong(),TimeUnit.SECONDS)
        .retryOnConnectionFailure(true).followRedirects(true).followSslRedirects(true)
        .addInterceptor { chain ->
            val builder=chain.request().newBuilder().header("User-Agent",config.userAgent.ifBlank{"CentralPlay/1.0"}).header("Accept","*/*")
            config.publicHeaders.forEach { (name,value) -> if(name.isNotBlank()&&value.isNotBlank()) builder.header(name,value) }
            chain.proceed(builder.build())
        }.build()

    fun retrofit(client:OkHttpClient):Retrofit=Retrofit.Builder().baseUrl("https://localhost/").client(client).addConverterFactory(ScalarsConverterFactory.create()).build()
}
