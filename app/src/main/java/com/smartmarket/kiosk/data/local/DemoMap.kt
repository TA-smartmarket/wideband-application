package com.smartmarket.kiosk.data.local

import com.smartmarket.kiosk.data.model.Destination
import com.smartmarket.kiosk.data.model.Edge
import com.smartmarket.kiosk.data.model.MapGraph
import com.smartmarket.kiosk.data.model.Node
import com.smartmarket.kiosk.data.model.Shelf

/**
 * Demo supermarket floor plan.
 *
 * This is a SIMULATION dataset — clearly labelled as such in the UI. Coordinates
 * are logical units (0..1000 x 0..1000). The graph is a 3-lane x 3-cross grid
 * providing multiple alternative routes so Dijkstra is meaningfully exercised.
 *
 * Replacing this with a real floor plan only requires swapping this object; the
 * map UI and Dijkstra are driven entirely by [MapGraph].
 */
object DemoMap {

    // Node layout (x=column corridor, y=row corridor). start/exit on the x=250 row.
    private fun node(id: String, x: Float, y: Float, label: String) = Node(id, x, y, label)

    val nodes: List<Node> = listOf(
        node("start", 80f, 500f, "Titik Awal"),
        node("n1", 250f, 300f, "Rak Sayur"),
        node("n2", 250f, 500f, "Lorong Tengah"),
        node("n3", 250f, 700f, "Rak Minuman"),
        node("n4", 450f, 300f, "Rak Daging"),
        node("n5", 450f, 500f, "Lorong Tengah"),
        node("n6", 450f, 700f, "Rak Snack"),
        node("n7", 650f, 300f, "Rak Sembako"),
        node("n8", 650f, 500f, "Lorong Tengah"),
        node("n9", 650f, 700f, "Rak Susu & Telur"),
        node("exit", 850f, 500f, "Kasir / Exit"),
    )

    val edges: List<Edge> = listOf(
        // Main corridor (y=500) + entrance/exit spurs.
        Edge("start", "n2", 170),
        Edge("n2", "n5", 200),
        Edge("n5", "n8", 200),
        Edge("n8", "exit", 200),
        // Vertical lanes.
        Edge("n1", "n2", 200),
        Edge("n2", "n3", 200),
        Edge("n4", "n5", 200),
        Edge("n5", "n6", 200),
        Edge("n7", "n8", 200),
        Edge("n8", "n9", 200),
        // Top (y=300) and bottom (y=700) horizontal aisles give alternative routes.
        Edge("n1", "n4", 200),
        Edge("n4", "n7", 200),
        Edge("n3", "n6", 200),
        Edge("n6", "n9", 200),
    )

    val shelves: List<Shelf> = listOf(
        Shelf("s1", "Sayur & Buah", 190f, 240f, 310f, 360f),
        Shelf("s2", "Minuman", 190f, 640f, 310f, 760f),
        Shelf("s3", "Daging & Seafood", 390f, 240f, 510f, 360f),
        Shelf("s4", "Snack & Roti", 390f, 640f, 510f, 760f),
        Shelf("s5", "Sembako", 590f, 240f, 710f, 360f),
        Shelf("s6", "Susu & Telur", 590f, 640f, 710f, 760f),
    )

    val destinations: List<Destination> = listOf(
        Destination("d_sayur", "Sayur & Buah", "n1", "s1"),
        Destination("d_minuman", "Minuman", "n3", "s2"),
        Destination("d_daging", "Daging & Seafood", "n4", "s3"),
        Destination("d_snack", "Snack & Roti", "n6", "s4"),
        Destination("d_sembako", "Sembako", "n7", "s5"),
        Destination("d_susu", "Susu & Telur", "n9", "s6"),
        Destination("d_kasir", "Kasir / Exit", "exit", null),
    )

    val graph: MapGraph = MapGraph(
        nodes = nodes,
        edges = edges,
        shelves = shelves,
        destinations = destinations,
        startNodeId = "start",
    )
}
