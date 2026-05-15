package com.shakilla.penjualan.kategori

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shakilla.penjualan.R
import com.shakilla.penjualan.model.ModelKategori
import java.util.*
import kotlin.collections.ArrayList

class KategoriAdapter(
    private var listKategoriFull: ArrayList<ModelKategori>,
    private val onItemClick: (ModelKategori) -> Unit
) : RecyclerView.Adapter<KategoriAdapter.KategoriViewHolder>() {

    // List yang akan ditampilkan ke layar
    private var listKategoriDisplay: List<ModelKategori> = listKategoriFull

    class KategoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaKategori)
        val tvStatus: TextView = itemView.findViewById(R.id.chipStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_kategori, parent, false)
        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(holder: KategoriViewHolder, position: Int) {
        val kategori = listKategoriDisplay[position]

        // Gunakan Safe Call ?. untuk menghindari crash jika data null
        holder.tvNama.text = kategori.namaKategori ?: "-"
        holder.tvStatus.text = if (kategori.statusKategori == "1") "Aktif" else "Tidak Aktif"

        holder.itemView.setOnClickListener { onItemClick(kategori) }
    }

    override fun getItemCount(): Int = listKategoriDisplay.size

    // Fungsi untuk update data dari Firebase
    fun updateData(newList: ArrayList<ModelKategori>) {
        listKategoriFull = newList
        listKategoriDisplay = newList
        notifyDataSetChanged()
    }

    // Fungsi Pencarian
    fun filter(query: String) {
        val pattern = query.lowercase(Locale.getDefault()).trim()
        listKategoriDisplay = if (pattern.isEmpty()) {
            listKategoriFull
        } else {
            listKategoriFull.filter {
                it.namaKategori?.lowercase(Locale.getDefault())?.contains(pattern) ?: false
            }
        }
        notifyDataSetChanged()
    }
}