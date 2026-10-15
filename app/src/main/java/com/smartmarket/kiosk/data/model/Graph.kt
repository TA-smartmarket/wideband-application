package com.smartmarket.kiosk.data.model

/**
 * Navigation graph model. Coordinates are LOGICAL units (0..1000), never pixels;
 * the UI maps them onto the canvas (see master prompt §6E).
 */
data class Node(
    val id: String,
    val x: Float,
    val y: Float,
    val label: String,
)

/** Undirected weighted edge between two nodes. Weights MUST be non-negative. */
data class Edge(
    val from: String,
    val to: String,
    val weight: Int,
)

/** A shelf/rack rectangle drawn on the floor plan (logical coordinates). */
data class Shelf(
    val id: String,
    val label: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

/** A selectable destination tied to a graph node (and optionally a shelf). */
data class Destination(
    val id: String,
    val label: String,
    val nodeId: String,
    val shelfId: String? = null,
)

/**
 * Immutable demo floor plan. The whole plan is defined in data, not inside the
 * UI composable.
 */
data class MapGraph(
    val nodes: List<Node>,
    val edges: List<Edge>,
    val shelves: List<Shelf>,
    val destinations: List<Destination>,
    val startNodeId: String,
) {
    val nodeById: Map<String, Node> = nodes.associateBy { it.id }
    val destinationById: Map<String, Destination> = destinations.associateBy { it.id }
    val shelfById: Map<String, Shelf> = shelves.associateBy { it.id }
}

/** A resolved shortest path. */
data class Route(
    val nodeIds: List<String>,
    val totalWeight: Int,
) {
    val isEmpty: Boolean get() = nodeIds.isEmpty()
}
