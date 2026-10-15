package com.smartmarket.kiosk.data.algorithm

import com.smartmarket.kiosk.data.model.MapGraph
import com.smartmarket.kiosk.data.model.Route

/**
 * Result of a shortest-path query. Sealed so callers handle every case without
 * leaking exceptions into the UI.
 */
sealed interface PathResult {
    data class Success(val route: Route) : PathResult

    /** Target node exists but is not reachable from the start node. */
    data object Unreachable : PathResult

    /** Start or target node id is not present in the graph. */
    data object InvalidNode : PathResult

    /** Graph has no nodes (or start/target missing), nothing to compute. */
    data object EmptyGraph : PathResult
}

/**
 * Dijkstra shortest path over a non-negative-weight undirected graph.
 *
 * Pure Kotlin, no Android/UI dependencies — kept out of composables so it can be
 * unit-tested directly (master prompt §6F).
 */
object Dijkstra {

    /**
     * Returns the shortest path from [startId] to [targetId].
     *
     * Invariants:
     * - Negative edge weights are treated as invalid input (they break Dijkstra);
     *   we clamp by rejecting via [IllegalArgumentException] only for explicit
     *   misuse. The demo graph contains none.
     * - A start == target yields a single-node route with total weight 0.
     */
    fun shortestPath(graph: MapGraph, startId: String, targetId: String): PathResult {
        if (graph.nodes.isEmpty()) return PathResult.EmptyGraph

        val nodeIds = graph.nodeById.keys
        if (startId !in nodeIds || targetId !in nodeIds) return PathResult.InvalidNode

        // Build adjacency once.
        val adjacency = HashMap<String, MutableList<Pair<String, Int>>>()
        for (edge in graph.edges) {
            require(edge.weight >= 0) { "Edge weight must be non-negative" }
            adjacency.getOrPut(edge.from) { mutableListOf() }.add(edge.to to edge.weight)
            adjacency.getOrPut(edge.to) { mutableListOf() }.add(edge.from to edge.weight)
        }

        val dist = HashMap<String, Int>()
        val prev = HashMap<String, String>()
        val settled = HashSet<String>()

        for (id in nodeIds) dist[id] = Int.MAX_VALUE
        dist[startId] = 0

        // Simple O(V^2) min-extraction; the demo graph is small, this is
        // allocation-light and trivially correct.
        while (settled.size < nodeIds.size) {
            var current: String? = null
            var best = Int.MAX_VALUE
            for ((id, d) in dist) {
                if (id !in settled && d < best) {
                    best = d
                    current = id
                }
            }
            val u = current ?: break
            if (best == Int.MAX_VALUE) break // remaining nodes unreachable
            settled.add(u)
            if (u == targetId) break

            for ((v, w) in adjacency[u].orEmpty()) {
                if (v in settled) continue
                val candidate = if (best == Int.MAX_VALUE) Int.MAX_VALUE else best + w
                if (candidate < dist.getValue(v)) {
                    dist[v] = candidate
                    prev[v] = u
                }
            }
        }

        val finalDist = dist[targetId] ?: Int.MAX_VALUE
        if (finalDist == Int.MAX_VALUE) return PathResult.Unreachable

        // Reconstruct the route by walking backwards.
        val path = ArrayDeque<String>()
        var cursor: String? = targetId
        while (cursor != null) {
            path.addFirst(cursor)
            cursor = if (cursor == startId) null else prev[cursor]
        }

        return PathResult.Success(Route(nodeIds = path.toList(), totalWeight = finalDist))
    }
}
