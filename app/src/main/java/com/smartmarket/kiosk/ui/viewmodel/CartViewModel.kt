package com.smartmarket.kiosk.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.smartmarket.kiosk.data.model.CartItem
import com.smartmarket.kiosk.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Local cart (distinct from a confirmed backend order). Money stays integer
 * Rupiah; `total` is computed from item subtotals, never floating point.
 */
class CartViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    val total: Int get() = _items.value.sumOf { it.subtotal }

    val itemCount: Int get() = _items.value.sumOf { it.quantity }

    fun add(product: Product, quantity: Int = 1) {
        if (quantity <= 0) return
        _items.update { items ->
            val existing = items.indexOfFirst { it.id == product.id }
            if (existing >= 0) {
                items.mapIndexed { i, item ->
                    if (i == existing) item.copy(quantity = item.quantity + quantity) else item
                }
            } else {
                items + CartItem(
                    id = product.id,
                    name = product.name,
                    quantity = quantity,
                    unitPrice = product.price,
                    emoji = product.emoji,
                )
            }
        }
    }

    fun increment(id: String) {
        _items.update { items ->
            items.map { if (it.id == id) it.copy(quantity = it.quantity + 1) else it }
        }
    }

    fun decrement(id: String) {
        _items.update { items ->
            items.map { if (it.id == id) it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) else it }
        }
    }

    fun remove(id: String) {
        _items.update { items -> items.filterNot { it.id == id } }
    }

    fun clear() {
        _items.value = emptyList()
    }
}
