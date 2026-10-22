package com.smartmarket.kiosk.data.repository

import com.smartmarket.kiosk.data.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Where the cart contents come from. `NONE` means no authoritative source yet. */
enum class CartSource { NONE, RFID }

/**
 * System-confirmed cart contents. `items` is the single source of truth for what
 * the trolley is currently carrying; quantities are assigned by the RFID/backend
 * system, never by the user.
 */
data class CartData(
    val items: List<CartItem> = emptyList(),
    val source: CartSource = CartSource.NONE,
)

/**
 * Authoritative cart data source (RFID/backend). This is the seam a real RFID
 * integration replaces; until that contract exists, [PendingCartRepository]
 * emits an empty cart and the UI shows "menunggu data RFID" — it never invents
 * transactions.
 */
interface CartRepository {
    val state: StateFlow<CartData>
}

/**
 * No RFID/backend contract is confirmed yet, so the cart starts empty and is
 * clearly labelled as awaiting RFID. A real adapter swaps in behind the same
 * [CartRepository] seam without touching the UI.
 */
class PendingCartRepository : CartRepository {
    private val _state = MutableStateFlow(CartData())
    override val state: StateFlow<CartData> = _state.asStateFlow()
}