package com.smartmarket.kiosk.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartmarket.kiosk.data.model.Product
import com.smartmarket.kiosk.databinding.ItemProductBinding
import com.smartmarket.kiosk.util.formatRupiah

/** Grid-friendly product card adapter (2 columns via GridLayoutManager). */
class ProductAdapter(
    private val onClick: (Product) -> Unit,
) : ListAdapter<Product, ProductAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class Holder(
        private val binding: ItemProductBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val item = getItem(bindingAdapterPosition)
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) onClick(item)
            }
        }

        fun bind(product: Product) {
            binding.tvEmoji.text = product.emoji
            binding.tvName.text = product.name
            binding.tvPrice.text = formatRupiah(product.price)
            binding.tvStock.text = "Stok ${product.stock}"
        }
    }

    private object Diff : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(a: Product, b: Product) = a.id == b.id
        override fun areContentsTheSame(a: Product, b: Product) = a == b
    }
}