package com.shakilla.penjualan.pegawai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.shakilla.penjualan.Pegawai
import com.shakilla.penjualan.R

class PegawaiAdapter(
    private var listPegawai: MutableList<Pegawai>, // List yang akan ditampilkan di layar
    private val onEdit: (Pegawai) -> Unit,
    private val onHapus: (Pegawai) -> Unit
) : RecyclerView.Adapter<PegawaiAdapter.ViewHolder>() {

    // 1. List cadangan untuk menyimpan seluruh data asli dari Firebase
    private var listFull: List<Pegawai> = ArrayList(listPegawai)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFoto: ShapeableImageView = view.findViewById(R.id.ivFotoPegawai)
        val tvNama: TextView = view.findViewById(R.id.tvNamaPegawai)
        val tvJabatan: TextView = view.findViewById(R.id.tvJabatanPegawai)
        val tvTelp: TextView = view.findViewById(R.id.tvTelpPegawai)
        val btnEdit: ImageView = view.findViewById(R.id.btnEditPegawai)
        val btnHapus: ImageView = view.findViewById(R.id.btnHapusPegawai)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pegawai, parent, false))

    override fun getItemCount() = listPegawai.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pegawai = listPegawai[position]
        holder.tvNama.text = pegawai.nama
        holder.tvJabatan.text = pegawai.jabatan
        holder.tvTelp.text = pegawai.telp

        Glide.with(holder.itemView.context)
            .load(if (pegawai.fotoUrl.isNotEmpty()) pegawai.fotoUrl else R.drawable.ic_profile)
            .placeholder(R.drawable.ic_profile)
            .circleCrop()
            .into(holder.ivFoto)

        holder.btnEdit.setOnClickListener { onEdit(pegawai) }
        holder.btnHapus.setOnClickListener { onHapus(pegawai) }
    }

    // 2. Fungsi untuk memperbarui data dari Firebase (Panggil ini di Activity)
    fun updateData(newList: List<Pegawai>) {
        listPegawai.clear()
        listPegawai.addAll(newList)
        listFull = ArrayList(newList) // Sinkronkan data cadangan
        notifyDataSetChanged()
    }

    // 3. Fungsi Filter untuk pencarian
    fun filter(query: String) {
        val filteredList = if (query.isEmpty()) {
            listFull // Jika kolom pencarian kosong, tampilkan semua data asli
        } else {
            // Cari berdasarkan nama atau jabatan (tidak sensitif huruf besar/kecil)
            listFull.filter {
                it.nama.contains(query, ignoreCase = true) ||
                        it.jabatan.contains(query, ignoreCase = true)
            }
        }

        listPegawai.clear()
        listPegawai.addAll(filteredList)
        notifyDataSetChanged() // Refresh tampilan RecyclerView
    }
}