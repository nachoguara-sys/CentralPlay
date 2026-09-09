package com.centralplay.app.playback

data class PlaybackSource(
    val url:String,
    val headers:Map<String,String> = emptyMap(),
    val backupUrls:List<String> = emptyList(),
    val mimeType:String? = null,
    val expiresAtEpochSeconds:Long? = null
){
    fun allUrls():List<String> = buildList { if(url.isNotBlank())add(url); backupUrls.filter{it.isNotBlank()&&it!=url}.forEach(::add) }
}
