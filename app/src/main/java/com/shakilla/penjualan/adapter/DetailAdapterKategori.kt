package com.shakilla.penjualan.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.shakilla.penjualan.kategori.ModKategoriActivity
import com.shakilla.penjualan.R
import com.shakilla.penjualan.model.ModelKategori

class DetailAdapterKategori (private val kategoriList : List <ModelKategori>) :
    RecyclerView.Adapter<DetailAdapterKategori.KategoriViewHolder>(){
    lateinit var appContext: Context
    interface OnItemClickListener {
        fun onItemClick (kategori: ModelKategori)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener (listener : OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(

        parent: ViewGroup,

        viewType: Int
    ): KategoriViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_kategori, parent, false)
        appContext = parent.context
        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: KategoriViewHolder,
        position: Int) {

        val kategori = kategoriList[position]
        holder.bind(kategori)
    }

    override fun getItemCount(): Int {
        return  kategoriList.size
    }

    inner class KategoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaKategori: TextView = itemView.findViewById(R.id.tvNamaKategori)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)

        fun bind(kategori: ModelKategori) {
            tvNamaKategori.text = kategori.namaKategori // sesuaikan dengan field di modelmu

            itemView.setOnClickListener {
                listener?.onItemClick(kategori)
            }
        }
    }
}