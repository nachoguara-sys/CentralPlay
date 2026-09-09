package com.centralplay.app.catalog

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface CatalogService {
    @POST
    suspend fun getChannelsCatalog(
        @Url url: String,
        @Body requestBody: Map<String, String>
    ): Response<ChannelCatalogResponse>
}
