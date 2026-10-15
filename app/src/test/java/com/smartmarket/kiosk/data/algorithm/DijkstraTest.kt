package com.smartmarket.kiosk.data.algorithm

import com.smartmarket.kiosk.data.local.DemoMap
import com.smartmarket.kiosk.data.model.Edge
import com.smartmarket.kiosk.data.model.MapGraph
import com.smartmarket.kiosk.data.model.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DijkstraTest {

    private val demoGraph = DemoMap.graph

    // ── Simple route ────────────────────────────────────────────────────────
    @Test
    fun `simple route from start to n2 has weight 170`() {
        val result = Dijkstra.shortestPath(demoGraph, "start", "n2")
        assertTrue(result is PathResult.Success)
        val route = (result as PathResult.Success).route
        assertEquals(listOf("start", "n2"), route.nodeIds)
        assertEquals(170, route.totalWeight)
    }

    // ── Alternative routes / lowest total weight ────────────────────────────
    @Test
    fun `route to kasir takes lowest weight along main corridor`() {
        // start->n2(170)->n5(200)->n8(200)->exit(200) = 770
        val result = Dijkstra.shortestPath(demoGraph, "start", "exit")
        assertTrue(result is PathResult.Success)
        val route = (result as PathResult.Success).route
        assertEquals(listOf("start", "n2", "n5", "n8", "exit"), route.nodeIds)
        assertEquals(770, route.totalWeight)
    }

    @Test
    fun `route to top rack prefers the shorter spur`() {
        // start->n2(170)->n1(200) = 370 (vs longer detour through the aisle grid)
        val result = Dijkstra.shortestPath(demoGraph, "start", "n1")
        assertTrue(result is PathResult.Success)
        val route = (result as PathResult.Success).route
        assertEquals(370, route.totalWeight)
        assertEquals("n1", route.nodeIds.last())
    }

    // ── Start equals target ─────────────────────────────────────────────────
    @Test
    fun `start equals target yields single node zero weight`() {
        val result = Dijkstra.shortestPath(demoGraph, "start", "start")
        assertTrue(result is PathResult.Success)
        val route = (result as PathResult.Success).route
        assertEquals(listOf("start"), route.nodeIds)
        assertEquals(0, route.totalWeight)
    }

    // ── Unreachable target ──────────────────────────────────────────────────
    @Test
    fun `disconnected graph returns unreachable`() {
        val graph = MapGraph(
            nodes = listOf(Node("a", 0f, 0f, "A"), Node("b", 1f, 1f, "B"), Node("c", 2f, 2f, "C")),
            edges = listOf(Edge("a", "b", 1)),
            shelves = emptyList(),
            destinations = emptyList(),
            startNodeId = "a",
        )
        val result = Dijkstra.shortestPath(graph, "a", "c")
        assertTrue(result is PathResult.Unreachable)
    }

    // ── Invalid node ids ────────────────────────────────────────────────────
    @Test
    fun `invalid start node returns invalidNode`() {
        val result = Dijkstra.shortestPath(demoGraph, "nope", "exit")
        assertTrue(result is PathResult.InvalidNode)
    }

    @Test
    fun `invalid target node returns invalidNode`() {
        val result = Dijkstra.shortestPath(demoGraph, "start", "nope")
        assertTrue(result is PathResult.InvalidNode)
    }

    // ── Empty graph ─────────────────────────────────────────────────────────
    @Test
    fun `empty graph returns emptyGraph`() {
        val graph = MapGraph(
            nodes = emptyList(),
            edges = emptyList(),
            shelves = emptyList(),
            destinations = emptyList(),
            startNodeId = "start",
        )
        val result = Dijkstra.shortestPath(graph, "start", "exit")
        assertTrue(result is PathResult.EmptyGraph)
    }

    // ── Negative edge weight rejected ───────────────────────────────────────
    @Test(expected = IllegalArgumentException::class)
    fun `negative edge weight is rejected`() {
        val graph = MapGraph(
            nodes = listOf(Node("a", 0f, 0f, "A"), Node("b", 1f, 1f, "B")),
            edges = listOf(Edge("a", "b", -1)),
            shelves = emptyList(),
            destinations = emptyList(),
            startNodeId = "a",
        )
        Dijkstra.shortestPath(graph, "a", "b")
    }

    // ── Demo graph integrity ────────────────────────────────────────────────
    @Test
    fun `every destination in demo map has a reachable route`() {
        for (dest in demoGraph.destinations) {
            val result = Dijkstra.shortestPath(demoGraph, demoGraph.startNodeId, dest.nodeId)
            assertTrue("destination ${dest.id} should be reachable", result is PathResult.Success)
        }
    }
}
