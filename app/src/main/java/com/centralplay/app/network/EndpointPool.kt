package com.centralplay.app.network

import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class EndpointPool(
    rawNodes: List<String>,
    private val cooldownMillis: Long = 30_000L,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val nodes = rawNodes.mapNotNull(::normalize).distinct()
    private val index = AtomicInteger(0)
    private val unhealthyUntil = ConcurrentHashMap<String, Long>()

    fun isEmpty(): Boolean = nodes.isEmpty()
    fun size(): Int = nodes.size
    fun all(): List<String> = nodes.toList()

    fun current(): String? {
        if (nodes.isEmpty()) return null
        val start = Math.floorMod(index.get(), nodes.size)
        repeat(nodes.size) { offset ->
            val i = (start + offset) % nodes.size
            val node = nodes[i]
            if (isAvailable(node)) { index.set(i); return node }
        }
        return nodes[start]
    }

    @Synchronized fun rotate(): String? {
        if (nodes.isEmpty()) return null
        val start = Math.floorMod(index.incrementAndGet(), nodes.size)
        repeat(nodes.size) { offset ->
            val i = (start + offset) % nodes.size
            val node = nodes[i]
            if (isAvailable(node)) { index.set(i); return node }
        }
        return nodes[start]
    }

    fun markFailed(node: String?) { if (node != null && node in nodes) unhealthyUntil[node] = clock() + cooldownMillis }
    fun markHealthy(node: String?) { if (node != null) { unhealthyUntil.remove(node); nodes.indexOf(node).takeIf { it >= 0 }?.let(index::set) } }
    fun resetToPrimary() { if (nodes.isNotEmpty()) index.set(0) }
    fun resolve(path: String): String? { val base=current()?:return null; val clean=if(path.startsWith('/'))path else "/$path"; return base.trimEnd('/')+clean }
    private fun isAvailable(node: String): Boolean = (unhealthyUntil[node] ?: 0L) <= clock()
    private fun normalize(input: String): String? = try {
        val trimmed=input.trim().trimEnd('/'); val uri=URI(trimmed)
        when(uri.scheme?.lowercase()){ "http","https","ws","wss" -> if(uri.host.isNullOrBlank()) null else trimmed; else -> null }
    } catch(_:Exception){ null }
}
