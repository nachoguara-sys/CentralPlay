package com.centralplay.app.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.QueryMap
import retrofit2.http.Url

/** Dynamic endpoint interface. @Url allows node failover without rebuilding Retrofit. */
interface CentralApiService {
    @GET
    fun getText(
        @Url url: String,
        @HeaderMap headers: Map<String, String> = emptyMap(),
        @QueryMap query: Map<String, String> = emptyMap()
    ): Call<String>
}
