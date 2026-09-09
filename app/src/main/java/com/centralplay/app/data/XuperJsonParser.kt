package com.centralplay.app.data

import com.centralplay.app.model.HydraChannelItem
import com.centralplay.app.model.HydraChannelResponse
import com.google.gson.Gson

object XuperJsonParser {
    private val gson = Gson()
    fun parseCatalog(rawJson: String?): List<HydraChannelItem> = try {
        if (rawJson.isNullOrBlank()) emptyList()
        else gson.fromJson(rawJson, HydraChannelResponse::class.java)?.data?.channels.orEmpty()
    } catch (_: Exception) {
        emptyList()
    }
}
