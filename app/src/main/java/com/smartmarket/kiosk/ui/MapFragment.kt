package com.smartmarket.kiosk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.Chip
import com.smartmarket.kiosk.appContainer
import com.smartmarket.kiosk.data.uwb.statusLabel
import com.smartmarket.kiosk.data.uwb.qualityLabel
import com.smartmarket.kiosk.data.uwb.staleLabel
import com.smartmarket.kiosk.databinding.FragmentMapBinding
import com.smartmarket.kiosk.integration.TrolleyStatus
import com.smartmarket.kiosk.integration.TrolleySource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Map + navigation screen: demo floor plan, destination picker, route, and two
 * independently-rendered status lines — positioning (UWB) and navigation
 * (trolley). Demo and real UWB are labelled distinctly; no fake coordinates.
 */
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val navigator get() = requireActivity() as com.smartmarket.kiosk.ShopNavigator

    private var selectedDestinationId: String? = null

    private val graph get() = com.smartmarket.kiosk.data.local.DemoMap.graph

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSource.text = "Denah: DEMO · Posisi: Tidak terhubung"

        binding.mapView.setGraph(graph)

        buildDestinationChips()

        binding.btnStart.setOnClickListener {
            val destId = selectedDestinationId ?: return@setOnClickListener
            computeAndStart(destId)
        }
        binding.btnCancel.setOnClickListener {
            appContainer.trolleyRepository.cancel()
            binding.mapView.clearSimPosition()
            binding.mapView.setRoute(null)
            binding.btnStart.isEnabled = selectedDestinationId != null
            binding.tvRouteInfo.visibility = View.GONE
            binding.tvNavStatus.text = "Navigasi dibatalkan (simulasi)"
        }

        // Auto-navigate arriving from detail/cart ("Cari di Peta").
        val pending = (requireActivity() as com.smartmarket.kiosk.MainActivity)
            .consumePendingMapProduct()
        if (pending != null) {
            autoNavigateToProduct(pending)
        }

        observeTrolley()
        observeUwb()
    }

    private fun autoNavigateToProduct(productId: String) {
        val location = com.smartmarket.kiosk.data.local.DemoCatalog.locationByProduct[productId]
        if (location == null) {
            binding.tvNavStatus.text = "Lokasi produk belum tersedia"
            binding.tvRouteInfo.visibility = View.GONE
            return
        }
        selectedDestinationId = location.destinationId
        reflectSelection(location.destinationId)
        computeRoute(location.destinationId)
    }

    private fun buildDestinationChips() {
        binding.chipDestinations.removeAllViews()
        val destinations = com.smartmarket.kiosk.data.local.DemoMap.destinations
        destinations.forEach { dest ->
            val chip = Chip(requireContext()).apply {
                text = dest.label
                isCheckable = true
                setOnClickListener {
                    selectedDestinationId = dest.id
                    reflectSelection(dest.id)
                    computeRoute(dest.id)
                }
            }
            binding.chipDestinations.addView(chip)
        }
    }

    private fun reflectSelection(destinationId: String) {
        val destinations = com.smartmarket.kiosk.data.local.DemoMap.destinations
        for (i in 0 until binding.chipDestinations.childCount) {
            val chip = binding.chipDestinations.getChildAt(i) as? Chip ?: continue
            chip.isChecked = destinations.getOrNull(i)?.id == destinationId
        }
    }

    private fun computeRoute(destinationId: String) {
        val dest = graph.destinationById[destinationId]
        if (dest == null) {
            binding.btnStart.isEnabled = false
            binding.tvRouteInfo.visibility = View.GONE
            binding.tvNavStatus.text = "Tujuan tidak valid"
            return
        }
        val result = com.smartmarket.kiosk.data.algorithm.Dijkstra.shortestPath(
            graph, graph.startNodeId, dest.nodeId,
        )
        val route = when (result) {
            is com.smartmarket.kiosk.data.algorithm.PathResult.Success -> result.route
            is com.smartmarket.kiosk.data.algorithm.PathResult.InvalidNode -> {
                binding.btnStart.isEnabled = false
                binding.mapView.setRoute(null)
                binding.tvRouteInfo.visibility = View.GONE
                binding.tvNavStatus.text = "Tujuan tidak ditemukan di graf navigasi"
                return
            }

            else -> null
        }
        binding.mapView.setRoute(route)
        if (route == null) {
            binding.btnStart.isEnabled = false
            binding.tvRouteInfo.visibility = View.GONE
            binding.tvNavStatus.text = "Tujuan tidak dapat dijangkau"
            return
        }
        binding.btnStart.isEnabled = true
        binding.tvNavStatus.text = "Tujuan: ${dest.label}"
        binding.tvRouteInfo.text =
            "Rute: ${route.nodeIds.size} tahap · jarak estimasi ${route.totalWeight} unit"
        binding.tvRouteInfo.visibility = View.VISIBLE
    }

    private fun computeAndStart(destinationId: String) {
        val dest = graph.destinationById[destinationId] ?: return
        val result = com.smartmarket.kiosk.data.algorithm.Dijkstra.shortestPath(
            graph, graph.startNodeId, dest.nodeId,
        )
        val route = (result as? com.smartmarket.kiosk.data.algorithm.PathResult.Success)
            ?.route ?: return
        appContainer.trolleyRepository.startNavigation(route.nodeIds, dest.nodeId)
    }

    private fun observeTrolley() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appContainer.trolleyRepository.state.collectLatest { state ->
                    renderTrolley(state)
                }
            }
        }
    }

    private fun renderTrolley(state: com.smartmarket.kiosk.integration.TrolleyState) {
        val nodeId = state.currentNodeId
        if (state.status == TrolleyStatus.NAVIGATING || state.status == TrolleyStatus.ARRIVED) {
            val node = nodeId?.let { graph.nodeById[it] }
            if (node != null) {
                binding.mapView.setSimPosition(node.x, node.y)
            }
        } else if (state.status == TrolleyStatus.DEMO || state.source == TrolleySource.NONE) {
            binding.mapView.clearSimPosition()
        }

        val hasRoute = state.route.isNotEmpty()
        val routeStatus = when (state.status) {
            TrolleyStatus.NAVIGATING -> "Menavigasi (simulasi)"
            TrolleyStatus.ARRIVED -> "Tiba di tujuan (simulasi)"
            TrolleyStatus.POSITION_LOST -> "Kehilangan posisi — berhenti aman"
            else -> state.message ?: "Belum ada perintah navigasi"
        }
        binding.tvNavStatus.text = "Status troli: $routeStatus"

        if (!hasRoute) {
            binding.btnStart.isEnabled = selectedDestinationId != null
        } else {
            binding.btnStart.isEnabled = false
        }
    }

    private fun observeUwb() {
        val uwb = appContainer.uwbRepository
        uwb.start()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                uwb.state.collectLatest { s ->
                    renderUwb(s)
                }
            }
        }
    }

    private fun renderUwb(s: com.smartmarket.kiosk.data.uwb.UwbState) {
        binding.tvPositionStatus.text = buildString {
            append("Posisi troli: ")
            append(s.statusLabel())
        }

        if (s.x != null && s.y != null && s.tagOnline && !s.stale) {
            binding.tvCoords.text = buildString {
                append(String.format(java.util.Locale.US, "Koordinat: (%.2f m, %.2f m)", s.x, s.y))
                val q = s.qualityLabel()
                if (q.isNotEmpty()) append(" · ").append(q)
            }
            binding.mapView.setRealPosition(s.x, s.y)
        } else {
            binding.tvCoords.text = "Koordinat tidak tersedia" + s.staleLabel()
            binding.mapView.setRealPosition(null, null)
        }

        binding.mapView.setRoomSize(s.roomWidthM, s.roomHeightM)
        binding.tvSource.text =
            "Denah: DEMO · Posisi: " + s.statusLabel()
    }

    override fun onStop() {
        super.onStop()
        appContainer.uwbRepository.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}