package com.centralplay.app.model

import com.google.gson.annotations.SerializedName

data class HydraChannelResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("msg") val message: String?,
    @SerializedName("data") val data: HydraCatalogData?
)

data class HydraCatalogData(
    @SerializedName("categories") val categories: List<HydraCategory>?,
    @SerializedName("channels") val channels: List<HydraChannelItem>?
)

data class HydraCategory(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

data class HydraChannelItem(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("logo") val logoUrl: String?,
    @SerializedName("media_code") val mediaCode: String,
    @SerializedName("category_id") val categoryId: String?
)
