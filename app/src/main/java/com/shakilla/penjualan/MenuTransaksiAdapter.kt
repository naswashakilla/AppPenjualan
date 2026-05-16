package com.shakilla.penjualan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shakilla.penjualan.model.ModelMenu
import java.text.NumberFormat
import java.util.Locale

class MenuTransaksiAdapter(
    private val items: List<ModelMenu>,
    private val onTambah: (ModelMenu) -> Unit
) : RecyclerView.Adapter<MenuTransaksiAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivMenu    : ImageView = view.findViewById(R.id.ivMenu)
        val tvNama    : TextView  = view.findViewById(R.id.tvNamaMenu)
        val tvHarga   : TextView  = view.findViewById(R.id.tvHargaMenu)
        val tvStok    : TextView  = view.findViewById(R.id.tvStokMenu)
        val btnTambah : Button    = view.findViewById(R.id.btnTambah)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_menu_transaksi, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val menu = items[position]

        holder.tvNama.text  = menu.namaProduk ?: "-"
        holder.tvHarga.text = formatRupiah(menu.harga)
        holder.tvStok.text  = "Stok: ${menu.stok ?: "0"}"

        // Load gambar dari URL (pakai Glide)
        if (!menu.urlFoto.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(menu.urlFoto)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.ivMenu)
        } else {
            holder.ivMenu.setImageResource(R.drawable.ic_launcher_background)
        }

        // Disable jika stok 0
        val stok = menu.stok
        holder.btnTambah.isEnabled = stok > 0
        holder.btnTambah.alpha = if (stok > 0) 1f else 0.4f

        holder.btnTambah.setOnClickListener { onTambah(menu) }
    }

    override fun getItemCount() = items.size

    private fun formatRupiah(amount: Long): String {
        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return fmt.format(amount).replace(",00", "")
    }
}