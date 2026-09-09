package com.centralplay.app.playback

class MockPlaybackResolver(private val demoUrl:String):PlaybackResolver {
    override suspend fun resolveLive(mediaCode:String):Result<PlaybackSource> {
        if(demoUrl.isBlank()) return Result.failure(IllegalStateException("No demo stream configured"))
        return Result.success(PlaybackSource(url=demoUrl))
    }
}
