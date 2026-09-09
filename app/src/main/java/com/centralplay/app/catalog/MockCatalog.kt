package com.centralplay.app.catalog

object MockCatalog {
    fun channels(): List<Channel> = listOf(
        Channel(
            id = "demo-1",
            name = "Canal Demo HD",
            logoUrl = null,
            mediaCode = "demo_channel_1",
            categoryId = "demo",
            categoryName = "Demo"
        )
    )
}
