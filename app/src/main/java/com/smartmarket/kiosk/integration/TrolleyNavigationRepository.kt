package com.smartmarket.kiosk.integration

import kotlinx.coroutines.flow.StateFlow

/**
 * Application-side trolley status. These are proposed UI states (master prompt
 * §6H) and are NOT a claim about real firmware status — they are only mapped to
 * firmware after the actual telemetry format is verified.
 */
enum class TrolleyStatus {
    DISCONNECTED,
    DEMO,
    READY,
    WAITING_DESTINATION,
    NAVIGATING,
    STOPPED,
    /** Navigation core lost a valid position and stopped safely (telemetry `POSITION_LOST`). */
    POSITION_LOST,
    ARRIVED,
    FAULT,
}

/** Where the trolley state comes from. Demo state is never presented as real. */
enum class TrolleySource {
    NONE,
    DEMO,
    HARDWARE,
}

data class TrolleyState(
    val status: TrolleyStatus = TrolleyStatus.DISCONNECTED,
    val source: TrolleySource = TrolleySource.NONE,
    val currentNodeId: String? = null,
    val targetNodeId: String? = null,
    val route: List<String> = emptyList(),
    val routeIndex: Int = 0,
    val message: String? = null,
    val lastUpdateMs: Long? = null,
)

/**
 * Abstraction between the app and the trolley navigation subsystem. This is the
 * single seam through which a real hardware adapter will eventually send
 * destinations and receive telemetry (master prompt §9). It MUST NOT issue motor
 * commands until a verified firmware protocol exists — the demo implementation
 * only advances a simulated marker.
 */
interface TrolleyNavigationRepository {
    val state: StateFlow<TrolleyState>

    /** Enter explicit demo mode (no hardware involved). */
    fun enterDemoMode()

    /** Begin navigating the given route toward [targetNodeId]. */
    fun startNavigation(route: List<String>, targetNodeId: String)

    /** Advance the simulated marker by one node. */
    fun step()

    fun pause()

    fun resume()

    fun reset()

    fun cancel()

    fun clearFault()
}
