package com.shakilla.penjualan.kategori

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
import java.util.*

class MenuAdapter(
    private var listMenuFull: List<ModelMenu>,
    // Tambahkan callback untuk aksi Edit dan Hapus
    private val onEdit: (ModelMenu) -> Unit,
    private val onHapus: (ModelMenu) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private var listMenuDisplay: List<ModelMenu> = listMenuFull

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivFoto: ImageView = itemView.findViewById(R.id.ivItemFoto)
        val tvNama: TextView = itemView.findViewById(R.id.tvItemNama)
        val tvHarga: TextView = itemView.findViewById(R.id.tvItemHarga)
        val tvStok: TextView = itemView.findViewById(R.id.tvItemStok)
        val tvCabang: TextView = itemView.findViewById(R.id.tvItemCabang)
        val tvKategori: TextView = itemView.findViewById(R.id.tvItemKategori)
        val chipStatus: Chip = itemView.findViewById(R.id.chipItemStatus)

        // Pastikan ID tombol ini ada di item_menu.xml
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEditMenu)
        val btnHapus: ImageView = itemView.findViewById(R.id.btnDeleteMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = listMenuDisplay[position]

        holder.tvNama.text = menu.namaProduk
        holder.tvHarga.text = "Rp ${menu.harga}"
        holder.tvStok.text = "Stok: ${menu.stok}"

        // Gunakan joinToString untuk menampilkan List Cabang
        holder.tvCabang.text = menu.listCabang?.joinToString(", ") ?: "Semua Cabang"
        holder.tvKategori.text = menu.kategori ?: "Tanpa Kategori"

        // Set Status
        if (menu.status == "1") {
            holder.chipStatus.text = "Aktif"
            holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_green_light)
        } else {
            holder.chipStatus.text = "Habis"
            holder.chipStatus.setChipBackgroundColorResource(android.R.color.holo_red_light)
        }

        // Load Gambar
        Glide.with(holder.itemView.context)
            .load(menu.urlFoto)
            .placeholder(R.drawable.ic_search)
            .error(R.drawable.ic_produk)
            .into(holder.ivFoto)


        holder.btnEdit.setOnClickListener { onEdit(menu) }
        holder.btnHapus.setOnClickListener { onHapus(menu) }
    }

    override fun getItemCount(): Int = listMenuDisplay.size

    fun updateData(newList: List<ModelMenu>) {
        listMenuFull = newList
        listMenuDisplay = newList
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val filterPattern = query.lowercase(Locale.getDefault()).trim()
        listMenuDisplay = if (filterPattern.isEmpty()) {
            listMenuFull
        } else {
            listMenuFull.filter {
                it.namaProduk?.lowercase(Locale.getDefault())?.contains(filterPattern) == true ||
                        it.kategori?.lowercase(Locale.getDefault())?.contains(filterPattern) == true
            }
        }
        notifyDataSetChanged()
    }
}