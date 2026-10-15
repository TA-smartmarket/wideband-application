package com.smartmarket.kiosk.data.repository

import com.smartmarket.kiosk.data.model.Category
import com.smartmarket.kiosk.data.model.Product

/** Source-agnostic catalog contract (demo now, backend later). */
interface CatalogRepository {
    val isRemote: Boolean
    suspend fun getCategories(): List<Category>
    suspend fun getProducts(query: String? = null, categoryId: String? = null): List<Product>
    suspend fun getProduct(id: String): Product?
}
