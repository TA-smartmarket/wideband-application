package com.smartmarket.kiosk.data.model

/**
 * Product catalog entry. Money is represented as integer Rupiah (no cents) so
 * that arithmetic never touches floating point (see project master prompt §6D).
 */
data class Product(
    val id: String,
    val name: String,
    val categoryId: String,
    val description: String,
    val price: Int,
    val stock: Int,
    val emoji: String,
    val imageUrl: String? = null,
)

data class Category(
    val id: String,
    val title: String,
    val emoji: String,
)

data class CartItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Int,
    val emoji: String? = null,
) {
    val subtotal: Int get() = quantity * unitPrice
}

/**
 * Maps a product to a physical/demo location on the floor plan. This is a demo
 * dataset; real shelf mappings replace it without changing the UI.
 */
data class ProductLocation(
    val productId: String,
    val shelfId: String,
    val destinationId: String,
    val nodeId: String,
)

/** Authenticated user profile (never contains the password). */
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
)
