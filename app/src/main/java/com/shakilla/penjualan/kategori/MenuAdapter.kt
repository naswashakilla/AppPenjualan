package com.shakilla.penjualan.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.shakilla.penjualan.R
import com.shakilla.penjualan.model.ModelMenu
class MenuAdapter(private var listMenu: List<ModelMenu>) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivFoto: ImageView = itemView.findViewById(R.id.ivItemFoto)
        val tvNama: TextView = itemView.findViewById(R.id.tvItemNama)
        val tvHarga: TextView = itemView.findViewById(R.id.tvItemHarga)
        val tvStok: TextView = itemView.findViewById(R.id.tvItemStok)
        val tvCabang: TextView = itemView.findViewById(R.id.tvItemCabang)
        val tvKategori: TextView=itemView.findViewById(R.id.tvItemKategori)
        val chipStatus: Chip = itemView.findViewById(R.id.chipItemStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = listMenu[position]

        holder.tvNama.text = menu.namaProduk
        holder.tvHarga.text = "Rp ${menu.harga}"
        holder.tvStok.text = "Stok: ${menu.stok}"
        holder.tvCabang.text = menu.cabang
        holder.tvKategori.text = menu.kategori

        // Set Status
        if (menu.status == "1") {
            holder.chipStatus.text = "Aktif"
            holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_green_light)
        } else {
            holder.chipStatus.text = "Habis"
            holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_red_light)
        }

        // Load Gambar menggunakan Glide
        Glide.with(holder.itemView.context)
            .load(menu.urlFoto)
            .placeholder(R.drawable.ic_search) // Gambar sementara saat loading
            .into(holder.ivFoto)
    }

    override fun getItemCount(): Int = listMenu.size

    fun updateData(newList: List<ModelMenu>) {
        listMenu = newList
        notifyDataSetChanged()
    }
}