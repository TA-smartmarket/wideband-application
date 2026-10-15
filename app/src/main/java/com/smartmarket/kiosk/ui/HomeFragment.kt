package com.smartmarket.kiosk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.smartmarket.kiosk.R
import com.smartmarket.kiosk.ShopNavigator
import com.smartmarket.kiosk.appContainer
import com.smartmarket.kiosk.data.local.DemoCatalog
import com.smartmarket.kiosk.databinding.FragmentHomeBinding
import com.smartmarket.kiosk.integration.TrolleyStatus
import com.smartmarket.kiosk.integration.TrolleySource
import com.smartmarket.kiosk.util.formatRupiah
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Shopping home: search, categories, promo, recommendations, trolley status. */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val navigator get() = requireActivity() as ShopNavigator

    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvGreeting.text = "Halo, Selamat Datang"
        binding.tvSubtitle.text = "Apa yang ingin kamu beli hari ini?"
        binding.tvPromo.text = "Promo: katalog demo lokal — harga tidak mencerminkan transaksi nyata"

        adapter = ProductAdapter(onClick = { product ->
            navigator.showProductDetail(product.id)
        })
        binding.rvRecommended.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecommended.adapter = adapter
        adapter.submitList(DemoCatalog.products.take(6))

        buildCategoryChips()
        renderTrolley()

        binding.btnEndSession.setOnClickListener { navigator.endSession() }
        binding.btnOpenMap.setOnClickListener { navigator.showMap() }
        binding.btnViewAll.setOnClickListener { navigator.showCatalog() }

        binding.etSearch.setOnEditorActionListener { _, actionId, event ->
            if (event != null && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
            ) {
                navigator.showCatalog(query = binding.etSearch.text.toString())
                true
            } else {
                false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appContainer.trolleyRepository.state.collectLatest { state ->
                    renderTrolleyState(state)
                }
            }
        }
    }

    private fun buildCategoryChips() {
        binding.chipGroup.removeAllViews()
        DemoCatalog.categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = "${category.emoji} ${category.title}"
                isCheckable = false
                setOnClickListener { navigator.showCatalog(categoryId = category.id) }
            }
            binding.chipGroup.addView(chip)
        }
    }

    private fun renderTrolley() {
        val state = appContainer.trolleyRepository.state.value
        renderTrolleyState(state)
    }

    private fun renderTrolleyState(state: com.smartmarket.kiosk.integration.TrolleyState) {
        val dotColor = when {
            state.status == TrolleyStatus.FAULT -> R.color.sm_error
            state.status == TrolleyStatus.NAVIGATING -> R.color.sm_green
            state.status == TrolleyStatus.ARRIVED -> R.color.sm_green
            else -> R.color.sm_muted
        }

        val label = when (state.source) {
            TrolleySource.DEMO -> "Simulasi"
            TrolleySource.HARDWARE -> "Hardware"
            TrolleySource.NONE -> "Tidak terhubung"
        }

        binding.tvTrolleyStatus.text = when (state.status) {
            TrolleyStatus.DISCONNECTED -> "Tidak terhubung"
            TrolleyStatus.DEMO -> "Siap (Mode Demo)"
            TrolleyStatus.READY -> "Siap"
            TrolleyStatus.WAITING_DESTINATION -> "Menunggu tujuan"
            TrolleyStatus.NAVIGATING -> "Menavigasi…"
            TrolleyStatus.STOPPED -> "Berhenti"
            TrolleyStatus.ARRIVED -> "Tiba di tujuan"
            TrolleyStatus.FAULT -> "Gangguan"
        }
        binding.tvTrolleyDetail.text = "Mode: $label · ${state.message ?: "belum ada perintah navigasi"}"
        binding.trolleyDot.background.setTint(ContextCompat.getColor(requireContext(), dotColor))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}