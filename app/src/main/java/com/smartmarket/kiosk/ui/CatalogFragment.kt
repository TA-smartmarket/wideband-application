package com.smartmarket.kiosk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.smartmarket.kiosk.appContainer
import com.smartmarket.kiosk.data.model.Category
import com.smartmarket.kiosk.data.model.Product
import com.smartmarket.kiosk.databinding.FragmentCatalogBinding
import com.smartmarket.kiosk.ShopNavigator
import kotlinx.coroutines.launch

/** Filterable product catalog with search and category chips (demo data). */
class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private val navigator get() = requireActivity() as ShopNavigator

    private lateinit var adapter: ProductAdapter
    private var categories: List<Category> = emptyList()
    private var query: String = ""
    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        query = arguments?.getString(ARG_QUERY).orEmpty()
        selectedCategory = arguments?.getString(ARG_CATEGORY)

        adapter = ProductAdapter(onClick = { product ->
            navigator.showProductDetail(product.id)
        })
        binding.rvCatalog.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvCatalog.adapter = adapter

        binding.tvSource.text = "Sumber: Data Demo"

        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.etSearch.setText(query)
        binding.etSearch.setOnEditorActionListener { _, _, event ->
            if (event != null && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                query = binding.etSearch.text.toString()
                load()
                true
            } else {
                false
            }
        }

        loadCategoriesAndProducts()
    }

    private fun loadCategoriesAndProducts() {
        binding.tvCatalogStatus.visibility = View.VISIBLE
        binding.tvCatalogStatus.text = "Memuat katalog…"

        lifecycleScope.launch {
            val repo = appContainer.catalogRepository
            categories = repo.getCategories()
            buildCategoryChips()
            val products = repo.getProducts(query, selectedCategory)
            adapter.submitList(products)

            binding.tvCatalogStatus.visibility =
                if (products.isEmpty()) View.VISIBLE else View.GONE
            if (products.isEmpty()) {
                binding.tvCatalogStatus.text = "Tidak ada produk yang cocok"
            }
        }
    }

    private fun buildCategoryChips() {
        binding.chipCategories.removeAllViews()

        val allChip = Chip(requireContext()).apply {
            text = "Semua"
            isCheckable = true
            isChecked = selectedCategory == null
            setOnClickListener {
                selectedCategory = null
                rebuildChipSelection()
                load()
            }
        }
        binding.chipCategories.addView(allChip)

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = "${category.emoji} ${category.title}"
                isCheckable = true
                isChecked = selectedCategory == category.id
                setOnClickListener {
                    selectedCategory = category.id
                    rebuildChipSelection()
                    load()
                }
            }
            binding.chipCategories.addView(chip)
        }
    }

    private fun rebuildChipSelection() {
        for (i in 0 until binding.chipCategories.childCount) {
            val view = binding.chipCategories.getChildAt(i) as? Chip ?: continue
            val category = categories.getOrNull(i - 1)
            view.isChecked = if (i == 0) selectedCategory == null
            else category?.id == selectedCategory
        }
    }

    private fun load() {
        lifecycleScope.launch {
            val repo = appContainer.catalogRepository
            val products = repo.getProducts(query, selectedCategory)
            adapter.submitList(products)
            binding.tvCatalogStatus.visibility =
                if (products.isEmpty()) View.VISIBLE else View.GONE
            if (products.isEmpty()) binding.tvCatalogStatus.text = "Tidak ada produk yang cocok"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_QUERY = "query"
        private const val ARG_CATEGORY = "category"

        fun newInstance(query: String? = null, categoryId: String? = null) =
            CatalogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_QUERY, query)
                    putString(ARG_CATEGORY, categoryId)
                }
            }
    }
}