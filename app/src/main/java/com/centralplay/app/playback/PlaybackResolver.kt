package com.centralplay.app.playback

interface PlaybackResolver { suspend fun resolveLive(mediaCode:String):Result<PlaybackSource> }
