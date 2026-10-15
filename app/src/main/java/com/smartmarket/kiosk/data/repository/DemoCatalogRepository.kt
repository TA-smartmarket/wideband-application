package com.smartmarket.kiosk.data.repository

import com.smartmarket.kiosk.data.local.DemoCatalog
import com.smartmarket.kiosk.data.model.Category
import com.smartmarket.kiosk.data.model.Product

/** Local demo catalog source. Explicitly labelled "Demo" in the UI. */
class DemoCatalogRepository : CatalogRepository {
    override val isRemote: Boolean = false

    override suspend fun getCategories(): List<Category> = DemoCatalog.categories

    override suspend fun getProducts(query: String?, categoryId: String?): List<Product> {
        var result = DemoCatalog.products
        if (!query.isNullOrBlank()) {
            result = result.filter { it.name.contains(query, ignoreCase = true) }
        }
        if (!categoryId.isNullOrBlank()) {
            result = result.filter { it.categoryId == categoryId }
        }
        return result
    }

    override suspend fun getProduct(id: String): Product? =
        DemoCatalog.products.firstOrNull { it.id == id }
}
