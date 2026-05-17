package com.shakilla.penjualan.kategori

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.shakilla.penjualan.R
import com.shakilla.penjualan.model.ModelKategori
import java.util.Locale
import android.graphics.Color

class KategoriAdapter(
    private var listKategori: MutableList<ModelKategori>,
    private val onEdit: (ModelKategori) -> Unit,
    private val onHapus: (ModelKategori) -> Unit
) : RecyclerView.Adapter<KategoriAdapter.KategoriViewHolder>() {

    private var listKategoriFull: ArrayList<ModelKategori> = ArrayList(listKategori)

    class KategoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaKategori)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEditKategori)
        val btnHapus: ImageView = itemView.findViewById(R.id.btnDeleteKategori)
        val tvStatus: Chip = itemView.findViewById(R.id.chipStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_kategori, parent, false)

        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(holder: KategoriViewHolder, position: Int) {
        val kategori = listKategori[position]

        holder.tvNama.text = kategori.namaKategori

        if (kategori.statusKategori == "1") {

            holder.tvStatus.text = "Aktif"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_aktif)
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"))

        } else {

            holder.tvStatus.text = "Tidak Aktif"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_nonaktif)
            holder.tvStatus.setTextColor(Color.parseColor("#C62828"))
        }

        holder.btnEdit.setOnClickListener {
            onEdit(kategori)
        }

        holder.btnHapus.setOnClickListener {
            onHapus(kategori)
        }
    }

    override fun getItemCount(): Int = listKategori.size

    fun updateData(newList: List<ModelKategori>) {
        listKategori.clear()
        listKategori.addAll(newList)

        listKategoriFull.clear()
        listKategoriFull.addAll(newList)

        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val keyword = query.lowercase(Locale.getDefault()).trim()

        listKategori.clear()

        if (keyword.isEmpty()) {
            listKategori.addAll(listKategoriFull)
        } else {
            val filteredList = listKategoriFull.filter {
                it.namaKategori.orEmpty()
                    .lowercase(Locale.getDefault())
                    .contains(keyword)
            }
            listKategori.addAll(filteredList)
        }

        notifyDataSetChanged()
    }
}