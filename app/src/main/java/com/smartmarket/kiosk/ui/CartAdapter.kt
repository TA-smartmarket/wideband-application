package com.smartmarket.kiosk.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartmarket.kiosk.data.model.CartItem
import com.smartmarket.kiosk.databinding.ItemCartBinding
import com.smartmarket.kiosk.util.formatRupiah

/**
 * Read-only cart line. Quantities are system-confirmed (RFID/backend), so there
 * are intentionally no +/−, remove, or add controls — the count is displayed,
 * never edited.
 */
class CartAdapter : ListAdapter<CartItem, CartAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class Holder(
        private val binding: ItemCartBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvEmoji.text = emojiFor(item)
            binding.tvName.text = item.name
            binding.tvPrice.text = "${formatRupiah(item.subtotal)} (×${item.quantity})"
            binding.tvQuantity.text = item.quantity.toString()
        }
    }

    private fun emojiFor(item: CartItem): String {
        // The cart carries name/price but not the catalog emoji; fall back to a
        // neutral symbol rather than inventing imagery for an unknown item.
        return item.emoji ?: "🛒"
    }

    private object Diff : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(a: CartItem, b: CartItem) = a.id == b.id
        override fun areContentsTheSame(a: CartItem, b: CartItem) = a == b
    }
}