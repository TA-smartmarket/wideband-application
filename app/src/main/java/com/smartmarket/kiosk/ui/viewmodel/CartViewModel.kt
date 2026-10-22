package com.smartmarket.kiosk.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.smartmarket.kiosk.data.repository.CartData
import com.smartmarket.kiosk.data.repository.CartRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Read-only projection of the system-confirmed cart. Item contents and
 * quantities come exclusively from a [CartRepository] (RFID/backend); there is
 * no user-facing path to add, remove, or change quantities. Money stays integer
 * Rupiah; `total`/`itemCount` are derived from item subtotals, never floating
 * point.
 */
class CartViewModel(repository: CartRepository) : ViewModel() {

    val state: StateFlow<CartData> = repository.state

    val total: Int get() = state.value.items.sumOf { it.subtotal }

    val itemCount: Int get() = state.value.items.sumOf { it.quantity }
}