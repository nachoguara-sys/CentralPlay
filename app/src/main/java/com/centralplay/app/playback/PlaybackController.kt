package com.centralplay.app.playback

interface PlayerManager {
    fun play(source: PlaybackSource)
    fun replaceSource(source: PlaybackSource)
}

class PlaybackController(
    private val resolver: PlaybackResolver,
    private val playerManager: PlayerManager
) {
    suspend fun start(channelMediaCode: String): Result<PlaybackSource> {
        val result = resolver.resolveLive(channelMediaCode)
        result.onSuccess { playerManager.play(it) }
        return result
    }
}
