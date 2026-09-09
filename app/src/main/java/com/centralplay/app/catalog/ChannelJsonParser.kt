package com.centralplay.app.catalog

import com.google.gson.Gson

/** Parses catalog JSON into the app's provider-neutral channel model. */
object ChannelJsonParser {
    private val gson = Gson()

    fun parseCatalog(rawJson: String?): List<Channel> {
        if (rawJson.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson(rawJson, ChannelCatalogResponse::class.java)
                ?.toChannels()
                .orEmpty()
        }.getOrElse { emptyList() }
    }
}
