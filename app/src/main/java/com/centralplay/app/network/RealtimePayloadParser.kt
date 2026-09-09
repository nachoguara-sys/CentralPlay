package com.centralplay.app.network

import com.google.gson.Gson
import com.google.gson.JsonObject

class RealtimePayloadParser(private val gson:Gson=Gson()) {
    data class Update(val type:String,val contentId:String?=null,val playlistUrl:String?=null,val catalogVersion:Long?=null)
    fun parse(payload:String):Update?=try{
        val root=gson.fromJson(payload,JsonObject::class.java)?:return null
        Update(root.get("type")?.asString?:"unknown",root.get("contentId")?.takeUnless{it.isJsonNull}?.asString,root.get("playlistUrl")?.takeUnless{it.isJsonNull}?.asString,root.get("catalogVersion")?.takeUnless{it.isJsonNull}?.asLong)
    }catch(_:Exception){null}
}
