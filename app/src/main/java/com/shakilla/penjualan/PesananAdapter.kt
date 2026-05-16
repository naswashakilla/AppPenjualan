package com.shakilla.penjualan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shakilla.penjualan.ItemPesanan
import java.text.NumberFormat
import java.util.Locale

class PesananAdapter(
    private val items: List<ItemPesanan>,
    private val onTambah: (Int) -> Unit,
    private val onKurang: (Int) -> Unit
) : RecyclerView.Adapter<PesananAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama   : TextView    = view.findViewById(R.id.tvNamaPesanan)
        val tvJumlah : TextView    = view.findViewById(R.id.tvJumlah)
        val tvHarga  : TextView    = view.findViewById(R.id.tvHargaItem)
        val btnPlus  : ImageButton = view.findViewById(R.id.btnTambah)
        val btnMin   : ImageButton = view.findViewById(R.id.btnKurang)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_pesanan, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvNama.text   = item.menu.namaProduk ?: "-"
        holder.tvJumlah.text = item.jumlah.toString()
        holder.tvHarga.text  = formatRupiah(item.subtotal)
        holder.btnPlus.setOnClickListener { onTambah(holder.adapterPosition) }
        holder.btnMin.setOnClickListener  { onKurang(holder.adapterPosition) }
    }

    override fun getItemCount() = items.size

    private fun formatRupiah(amount: Long): String {
        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return fmt.format(amount).replace(",00", "")
    }
}