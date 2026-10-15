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
import com.smartmarket.kiosk.databinding.FragmentCartBinding
import com.smartmarket.kiosk.util.formatRupiah
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Shopping cart: quantity controls, remove, total, then navigate to a rack. */
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

        adapter = CartAdapter(
            onIncrement = { cart.increment(it) },
            onDecrement = { cart.decrement(it) },
            onRemove = { cart.remove(it) },
        )
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = adapter

        binding.btnNavigateCart.setOnClickListener {
            val first = cart.items.value.firstOrNull()
            if (first != null) {
                navigator.showMap(navigateToProductId = first.id)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cart.items.collectLatest { items ->
                    adapter.submitList(items)
                    val empty = items.isEmpty()
                    binding.emptyView.visibility = if (empty) View.VISIBLE else View.GONE
                    binding.rvCart.visibility = if (empty) View.GONE else View.VISIBLE
                    binding.tvCount.text = "${cart.itemCount} barang"
                    binding.tvTotal.text = formatRupiah(cart.total)
                    binding.btnNavigateCart.isEnabled = !empty
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}