package com.centralplay.app.playback

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import okhttp3.OkHttpClient

object Media3PlayerFactory {
    @OptIn(UnstableApi::class)
    fun create(context: Context, source: PlaybackSource): ExoPlayer {
        val client=OkHttpClient.Builder().addInterceptor { chain ->
            val builder=chain.request().newBuilder()
            source.headers.forEach { (name,value) -> if(name.isNotBlank()&&value.isNotBlank()) builder.header(name,value) }
            chain.proceed(builder.build())
        }.build()
        val dataSourceFactory=OkHttpDataSource.Factory(client)
        val mediaItemBuilder=MediaItem.Builder().setUri(source.url)
        source.mimeType?.takeIf{it.isNotBlank()}?.let(mediaItemBuilder::setMimeType)
        val item=mediaItemBuilder.build()
        val player=ExoPlayer.Builder(context).build()
        val isHls=source.mimeType==MimeTypes.APPLICATION_M3U8 || source.url.substringBefore('?').endsWith(".m3u8",ignoreCase=true)
        if(isHls) player.setMediaSource(HlsMediaSource.Factory(dataSourceFactory).createMediaSource(item)) else player.setMediaItem(item)
        player.prepare(); player.playWhenReady=true; return player
    }
}
