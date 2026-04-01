package com.shakilla.penjualan.kategori

    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView
    import com.google.android.material.chip.Chip
    import com.shakilla.penjualan.R
    import com.shakilla.penjualan.model.ModelKategori

    class AdapterKategori(
        private val list: ArrayList<ModelKategori>) :
        RecyclerView.Adapter<AdapterKategori.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNama: TextView = itemView.findViewById(R.id.txtNamaKategori)
            val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_data_kategori, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = list[position]

            holder.tvNama.text = data.namaKategori
            holder.chipStatus.text = data.statusKategori
        }
    }