package com.centralplay.app.network

import com.centralplay.app.model.NetworkConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min
import kotlin.random.Random

class RealTimeSyncManager(private val config:NetworkConfig, private val onEvent:(RealtimeEvent)->Unit) {
    private val client:OkHttpClient=NetworkClientFactory.okHttp(config)
    private val pool=EndpointPool(config.webSocketNodes,config.nodeCooldownSeconds.coerceAtLeast(1)*1000L)
    private val scheduler=Executors.newSingleThreadScheduledExecutor()
    private val started=AtomicBoolean(false)
    private val connecting=AtomicBoolean(false)
    @Volatile private var socket:WebSocket?=null
    @Volatile private var reconnectAttempt=0
    @Volatile private var reconnectTask:ScheduledFuture<*>?=null

    fun start(){ if(pool.isEmpty()){onEvent(RealtimeEvent.Error("No WebSocket nodes configured"));return}; if(started.compareAndSet(false,true))connectCurrent() }
    fun stop(){ started.set(false); connecting.set(false); reconnectTask?.cancel(false); reconnectTask=null; socket?.close(1000,"Central Play stopping"); socket=null }
    fun send(text:String):Boolean=socket?.send(text)==true

    private fun connectCurrent(){
        if(!started.get()||!connecting.compareAndSet(false,true))return
        val target=pool.current()
        if(target==null){connecting.set(false);scheduleReconnect("No WebSocket node available");return}
        val request=try{Request.Builder().url(target).build()}catch(t:Throwable){connecting.set(false);pool.markFailed(target);pool.rotate();scheduleReconnect("Invalid WebSocket URL");return}
        socket=client.newWebSocket(request,object:WebSocketListener(){
            override fun onOpen(webSocket:WebSocket,response:Response){connecting.set(false);socket=webSocket;reconnectAttempt=0;pool.markHealthy(target);onEvent(RealtimeEvent.Connected(target))}
            override fun onMessage(webSocket:WebSocket,text:String){onEvent(RealtimeEvent.Payload(text))}
            override fun onClosing(webSocket:WebSocket,code:Int,reason:String){webSocket.close(code,reason);onEvent(RealtimeEvent.Disconnected(code,reason))}
            override fun onClosed(webSocket:WebSocket,code:Int,reason:String){socket=null;connecting.set(false);if(started.get())scheduleReconnect("Closed: $code")}
            override fun onFailure(webSocket:WebSocket,t:Throwable,response:Response?){socket=null;connecting.set(false);pool.markFailed(target);pool.rotate();onEvent(RealtimeEvent.Error(t.message?:"WebSocket failure"));if(started.get())scheduleReconnect(t.message?:"failure")}
        })
    }

    private fun scheduleReconnect(reason:String){
        if(!started.get())return
        reconnectTask?.cancel(false); reconnectAttempt++
        val base=config.baseReconnectSeconds.coerceAtLeast(1).toLong(); val cap=config.maxReconnectSeconds.coerceAtLeast(base.toInt()).toLong()
        val exponential=min(cap,base*(1L shl min(reconnectAttempt-1,5))); val jitterMillis=Random.nextLong(0L,1000L)
        reconnectTask=scheduler.schedule({if(started.get())connectCurrent()},exponential*1000L+jitterMillis,TimeUnit.MILLISECONDS)
        onEvent(RealtimeEvent.Error("Reconnect scheduled after $reason"))
    }
}
