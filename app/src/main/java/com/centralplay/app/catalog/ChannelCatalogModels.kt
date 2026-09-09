package com.centralplay.app.catalog

import com.google.gson.annotations.SerializedName

data class ChannelCatalogResponse(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("msg") val message: String? = null,
    @SerializedName("data") val data: CatalogData? = null
)

data class CatalogData(
    @SerializedName("categories") val categories: List<ChannelCategory>? = null,
    @SerializedName("channels") val channels: List<ChannelItem>? = null
)

data class ChannelCategory(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

data class ChannelItem(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("logo") val logoUrl: String? = null,
    @SerializedName("media_code") val mediaCode: String? = null,
    @SerializedName("category_id") val categoryId: String? = null
)

data class Channel(
    val id: String,
    val name: String,
    val logoUrl: String?,
    val mediaCode: String,
    val categoryId: String?,
    val categoryName: String?
)

fun ChannelCatalogResponse.toChannels(): List<Channel> {
    val catalog = data ?: return emptyList()
    val categoriesById = catalog.categories.orEmpty().associateBy { it.id }
    return catalog.channels.orEmpty()
        .filter { !it.mediaCode.isNullOrBlank() }
        .map { item ->
            Channel(
                id = item.id,
                name = item.name,
                logoUrl = item.logoUrl,
                mediaCode = item.mediaCode.orEmpty(),
                categoryId = item.categoryId,
                categoryName = item.categoryId?.let { categoriesById[it]?.name }
            )
        }
}
