package com.shakilla.penjualan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shakilla.penjualan.model.ModelCabang

class CabangAdapter(private val list: List<ModelCabang>) : RecyclerView.Adapter<CabangAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nama: TextView = view.findViewById(R.id.tvNamaCabangItem)
        val ket: TextView = view.findViewById(R.id.tvKeteranganCabangItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cabang, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.nama.text = item.namaCabang
        holder.ket.text = item.keterangan ?: "Tidak ada keterangan"
    }

    override fun getItemCount(): Int = list.size
}
