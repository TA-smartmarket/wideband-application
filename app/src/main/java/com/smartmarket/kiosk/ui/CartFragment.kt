package com.smartmarket.kiosk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartmarket.kiosk.ShopNavigator
import com.smartmarket.kiosk.appContainer
import com.smartmarket.kiosk.data.repository.CartSource
import com.smartmarket.kiosk.databinding.FragmentCartBinding
import com.smartmarket.kiosk.util.formatRupiah
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Read-only cart: item contents and quantities come from the system-confirmed
 * RFID/backend source. When that source has no data yet, the screen clearly
 * reports "menunggu data RFID" instead of showing empty transactions.
 */
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val navigator get() = requireActivity() as ShopNavigator

    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cart = appContainer.cartViewModel

        adapter = CartAdapter()
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = adapter

        binding.btnNavigateCart.setOnClickListener {
            val first = cart.state.value.items.firstOrNull()
            if (first != null) {
                navigator.showMap(navigateToProductId = first.id)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cart.state.collectLatest { data ->
                    adapter.submitList(data.items)

                    val pending = data.source == CartSource.NONE &&
                        cart.itemCount == 0
                    when {
                        pending -> {
                            binding.emptyView.visibility = View.VISIBLE
                            binding.rvCart.visibility = View.GONE
                            binding.tvEmptyTitle.text = "Data keranjang belum tersedia"
                            binding.tvEmptyMessage.text =
                                "Menunggu koneksi RFID — sistem akan mengisi keranjang saat barang terdeteksi"
                        }

                        data.items.isEmpty() -> {
                            binding.emptyView.visibility = View.VISIBLE
                            binding.rvCart.visibility = View.GONE
                            binding.tvEmptyTitle.text = getString(com.smartmarket.kiosk.R.string.empty_cart_title)
                            binding.tvEmptyMessage.text = getString(com.smartmarket.kiosk.R.string.empty_cart_body)
                        }

                        else -> {
                            binding.emptyView.visibility = View.GONE
                            binding.rvCart.visibility = View.VISIBLE
                        }
                    }

                    binding.tvCount.text = "${cart.itemCount} barang"
                    binding.tvTotal.text = formatRupiah(cart.total)
                    binding.btnNavigateCart.isEnabled = data.items.isNotEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}