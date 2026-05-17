package com.shakilla.penjualan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shakilla.penjualan.ModelTransaksi
import java.text.NumberFormat
import java.util.Locale

class RiwayatAdapter(
    private val list: List<ModelTransaksi>,
    private val onPrintClick: (ModelTransaksi) -> Unit,
    private val onDeleteClick: (ModelTransaksi) -> Unit
) : RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggalRiwayat)
        val tvItems: TextView = view.findViewById(R.id.tvListMenuRiwayat)
        val tvTotal: TextView = view.findViewById(R.id.tvTotalRiwayat)
        val btnPrint: ImageButton = view.findViewById(R.id.btnCetakUlang)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteRiwayat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        holder.tvTanggal.text = data.tanggal
        holder.tvTotal.text = formatRupiah(data.total)

        // Menggabungkan nama-nama produk untuk ditampilkan sekilas
        val ringkasanMenu = data.items?.joinToString(", ") {
            "${it["namaProduk"]} (${it["jumlah"]})"
        }
        holder.tvItems.text = ringkasanMenu ?: "-"

        holder.btnPrint.setOnClickListener { onPrintClick(data) }
        holder.btnDelete.setOnClickListener { onDeleteClick(data) }
    }

    override fun getItemCount(): Int = list.size

    private fun formatRupiah(number: Long): String {
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        return formatRupiah.format(number).replace(",00", "")
    }
}