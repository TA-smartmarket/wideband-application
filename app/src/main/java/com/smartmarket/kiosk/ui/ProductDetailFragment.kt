package com.smartmarket.kiosk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.smartmarket.kiosk.ShopNavigator
import com.smartmarket.kiosk.appContainer
import com.smartmarket.kiosk.data.local.DemoCatalog
import com.smartmarket.kiosk.data.model.Product
import com.smartmarket.kiosk.databinding.FragmentProductDetailBinding
import com.smartmarket.kiosk.util.formatRupiah
import kotlinx.coroutines.launch

/**
 * Product detail: description, price, shelf location, and map navigation. No
 * manual add-to-cart or quantity picker — cart contents are system-confirmed
 * (RFID), so selecting a destination must NOT add the product to the cart.
 */
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private val navigator get() = requireActivity() as ShopNavigator

    private var product: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        loadProduct(arguments?.getString(ARG_PRODUCT_ID))

        binding.btnNavigate.setOnClickListener {
            val p = product ?: return@setOnClickListener
            navigator.showMap(navigateToProductId = p.id)
        }
    }

    private fun loadProduct(productId: String?) {
        if (productId == null) {
            showNotAvailable()
            return
        }
        lifecycleScope.launch {
            val p = appContainer.catalogRepository.getProduct(productId)
            if (p == null) {
                showNotAvailable()
            } else {
                product = p
                render()
            }
        }
    }

    private fun showNotAvailable() {
        binding.tvName.text = "Produk tidak ditemukan"
        binding.tvPrice.text = ""
        binding.tvDescription.text = ""
        binding.tvEmoji.text = "?"
        binding.btnNavigate.isEnabled = false
    }

    private fun render() {
        val p = product ?: return
        binding.tvEmoji.text = p.emoji
        binding.tvName.text = p.name
        binding.tvDescription.text = p.description
        binding.tvPrice.text = formatRupiah(p.price)
        binding.tvStock.text = "Stok: ${p.stock}"

        val category = DemoCatalog.categories.firstOrNull { it.id == p.categoryId }
        binding.tvCategory.text = category?.title ?: p.categoryId

        val location = DemoCatalog.locationByProduct[p.id]
        val shelf = location?.let { com.smartmarket.kiosk.data.local.DemoMap.shelves.associateBy { it.id }[it.shelfId] }
        binding.tvLocation.text = if (shelf != null) "Rak: ${shelf.label}" else "Lokasi rak belum tersedia"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PRODUCT_ID = "productId"

        fun newInstance(productId: String) = ProductDetailFragment().apply {
            arguments = Bundle().apply { putString(ARG_PRODUCT_ID, productId) }
        }
    }
}