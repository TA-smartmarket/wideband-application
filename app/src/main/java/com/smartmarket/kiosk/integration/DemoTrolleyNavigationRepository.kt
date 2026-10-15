package com.smartmarket.kiosk.integration

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Deterministic in-memory trolley simulation. Advances a marker along a
 * precomputed route one node at a time. It NEVER sends motor commands and never
 * claims real hardware connectivity — the UI labels this "Simulasi".
 */
class DemoTrolleyNavigationRepository : TrolleyNavigationRepository {

    private val _state = MutableStateFlow(TrolleyState())
    override val state: StateFlow<TrolleyState> = _state.asStateFlow()

    override fun enterDemoMode() {
        _state.update {
            it.copy(
                status = TrolleyStatus.DEMO,
                source = TrolleySource.DEMO,
                route = emptyList(),
                routeIndex = 0,
                currentNodeId = null,
                targetNodeId = null,
                message = "Mode demo aktif — bukan posisi hardware",
            )
        }
    }

    override fun startNavigation(route: List<String>, targetNodeId: String) {
        if (route.isEmpty()) {
            _state.update {
                it.copy(
                    status = TrolleyStatus.FAULT,
                    source = TrolleySource.DEMO,
                    message = "Rute kosong",
                )
            }
            return
        }
        _state.update {
            it.copy(
                status = TrolleyStatus.NAVIGATING,
                source = TrolleySource.DEMO,
                route = route,
                routeIndex = 0,
                currentNodeId = route.first(),
                targetNodeId = targetNodeId,
                message = "Menavigasi (simulasi)",
                lastUpdateMs = System.currentTimeMillis(),
            )
        }
    }

    override fun step() {
        val s = _state.value
        if (s.status != TrolleyStatus.NAVIGATING) return
        val nextIndex = s.routeIndex + 1
        val arrived = nextIndex >= s.route.size
        _state.update {
            it.copy(
                routeIndex = if (arrived) s.route.lastIndex else nextIndex,
                currentNodeId = if (arrived) s.route.last() else s.route[nextIndex],
                status = if (arrived) TrolleyStatus.ARRIVED else TrolleyStatus.NAVIGATING,
                message = if (arrived) "Tiba di tujuan (simulasi)" else "Menavigasi (simulasi)",
                lastUpdateMs = System.currentTimeMillis(),
            )
        }
    }

    override fun pause() {
        _state.update {
            if (it.status == TrolleyStatus.NAVIGATING)
                it.copy(status = TrolleyStatus.STOPPED, message = "Berhenti (simulasi)")
            else it
        }
    }

    override fun resume() {
        _state.update {
            if (it.status == TrolleyStatus.STOPPED)
                it.copy(status = TrolleyStatus.NAVIGATING, message = "Menavigasi (simulasi)")
            else it
        }
    }

    override fun reset() {
        _state.update {
            it.copy(
                status = TrolleyStatus.DEMO,
                source = TrolleySource.DEMO,
                route = emptyList(),
                routeIndex = 0,
                currentNodeId = null,
                targetNodeId = null,
                message = "Mode demo aktif — bukan posisi hardware",
                lastUpdateMs = null,
            )
        }
    }

    override fun cancel() {
        _state.update {
            it.copy(
                status = TrolleyStatus.DEMO,
                route = emptyList(),
                routeIndex = 0,
                currentNodeId = null,
                targetNodeId = null,
                message = "Navigasi dibatalkan",
                lastUpdateMs = System.currentTimeMillis(),
            )
        }
    }

    override fun clearFault() {
        _state.update {
            if (it.status == TrolleyStatus.FAULT)
                it.copy(status = TrolleyStatus.DEMO, message = "Gangguan dibersihkan (simulasi)")
            else it
        }
    }
}
