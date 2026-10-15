package com.smartmarket.kiosk.integration

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * PLACEHOLDER hardware adapter. Implements the [TrolleyNavigationRepository]
 * seam but does NOT talk to any real firmware.
 *
 * The referenced firmware contracts (ESP32 UWB positioning, autonomous
 * navigation) were not available in this repo, so this adapter deliberately
 * makes no claim about real connectivity. It reports [TrolleySource.HARDWARE]
 * only when a real backend would have supplied telemetry; in this build it is
 * intentionally wired to the demo behaviour and every operation is a no-op that
 * records the requested intent in the state message.
 *
 * Swap this class for a real implementation once the actual telemetry protocol
 * is verified. Until then, it MUST NOT send motor commands.
 */
class HardwareTrolleyNavigationRepository : TrolleyNavigationRepository {

    private val _state = MutableStateFlow(
        TrolleyState(
            status = TrolleyStatus.DISCONNECTED,
            source = TrolleySource.HARDWARE,
            message = "Adapter hardware belum terhubung (kontrak firmware belum diverifikasi)",
        ),
    )
    override val state: StateFlow<TrolleyState> = _state.asStateFlow()

    override fun enterDemoMode() {
        // Demo mode is owned by DemoTrolleyNavigationRepository; a hardware
        // adapter never silently degrades to demo. Record the intent only.
        _state.update {
            it.copy(message = "enterDemoMode() dipanggil pada adapter hardware — diabaikan")
        }
    }

    override fun startNavigation(route: List<String>, targetNodeId: String) {
        // No motor command is issued. A verified protocol would map this to a
        // firmware destination command.
        _state.update {
            it.copy(
                route = route,
                targetNodeId = targetNodeId,
                message = "Permintaan navigasi dicatat (tidak ada perintah motor dikirim)",
                lastUpdateMs = System.currentTimeMillis(),
            )
        }
    }

    override fun step() {
        _state.update { it.copy(message = "step() diabaikan — telemetri hardware tidak tersedia") }
    }

    override fun pause() {
        _state.update { it.copy(message = "pause() diabaikan — telemetri hardware tidak tersedia") }
    }

    override fun resume() {
        _state.update { it.copy(message = "resume() diabaikan — telemetri hardware tidak tersedia") }
    }

    override fun reset() {
        _state.update {
            it.copy(
                status = TrolleyStatus.DISCONNECTED,
                route = emptyList(),
                routeIndex = 0,
                currentNodeId = null,
                targetNodeId = null,
                message = "Adapter hardware di-reset",
                lastUpdateMs = null,
            )
        }
    }

    override fun cancel() {
        _state.update {
            it.copy(
                route = emptyList(),
                routeIndex = 0,
                currentNodeId = null,
                targetNodeId = null,
                message = "Navigasi dibatalkan (tidak ada perintah motor dikirim)",
                lastUpdateMs = System.currentTimeMillis(),
            )
        }
    }

    override fun clearFault() {
        _state.update { it.copy(message = "clearFault() diabaikan — telemetri hardware tidak tersedia") }
    }
}
