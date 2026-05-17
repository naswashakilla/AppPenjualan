package com.shakilla.penjualan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shakilla.penjualan.model.ModelCabang

class CabangAdapter(private val list: List<ModelCabang>) : RecyclerView.Adapter<CabangAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nama: TextView = view.findViewById(android.R.id.text1)
        val ket: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Menggunakan layout bawaan Android (simple_list_item_2) agar cepat
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.nama.text = item.namaCabang
        holder.ket.text = item.keterangan ?: "Tidak ada keterangan"

        // Styling tambahan lewat kode
        holder.nama.textSize = 16f
        holder.nama.setTextColor(android.graphics.Color.BLACK)
    }

    override fun getItemCount(): Int = list.size
}