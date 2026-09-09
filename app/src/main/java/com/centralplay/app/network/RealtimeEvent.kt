package com.centralplay.app.network

sealed class RealtimeEvent {
    data class Connected(val host:String):RealtimeEvent()
    data class Payload(val text:String):RealtimeEvent()
    data class Disconnected(val code:Int,val reason:String):RealtimeEvent()
    data class Error(val message:String):RealtimeEvent()
}
