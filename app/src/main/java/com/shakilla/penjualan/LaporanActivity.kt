package com.shakilla.penjualan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.shakilla.penjualan.model.ModelPenjualan
import com.shakilla.penjualan.model.ModelMenu
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class LaporanActivity : AppCompatActivity() {

    private lateinit var spinnerTanggal: AutoCompleteTextView
    private lateinit var tvTotalProduk: TextView
    private lateinit var tvTotalNilai: TextView
    private lateinit var tvTotalKeuntungan: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var rvLaporan: RecyclerView
    private lateinit var layoutEmpty: View

    private lateinit var adapterLaporan: LaporanProdukAdapter
    private val listMenu = mutableListOf<ModelMenu>()

    // FIREBASE - Menggunakan node "menu" karena ModMenuActivity simpan ke sana
    private val dbMenu = FirebaseDatabase
        .getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("menu")

    private val dbPenjualan = FirebaseDatabase
        .getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("penjualan")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan)

        init()
        setupPeriode()

        // Default pertama kali
        loadBulanIni()
    }

    private fun init() {
        spinnerTanggal = findViewById(R.id.spinner_tanggal)
        tvTotalProduk = findViewById(R.id.tv_total_produk)
        tvTotalNilai = findViewById(R.id.tv_total_nilai)
        tvTotalKeuntungan = findViewById(R.id.tv_total_keuntungan)
        progressBar = findViewById(R.id.view_loading)
        layoutEmpty = findViewById(R.id.layout_empty)

        rvLaporan = findViewById(R.id.rv_laporan_produk)
        rvLaporan.layoutManager = LinearLayoutManager(this)
        adapterLaporan = LaporanProdukAdapter(listMenu)
        rvLaporan.adapter = adapterLaporan

        findViewById<View>(R.id.toolbar)?.setOnClickListener {
            finish()
        }
    }

    private fun setupPeriode() {
        val listPeriode = listOf("Hari Ini", "Minggu Ini", "Bulan Ini", "Tahun Ini")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listPeriode)
        spinnerTanggal.setAdapter(adapter)

        spinnerTanggal.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> loadHariIni()
                1 -> loadMingguIni()
                2 -> loadBulanIni()
                3 -> loadTahunIni()
            }
        }
    }

    private fun loadHariIni() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis
        loadLaporan(start, end)
    }

    private fun loadMingguIni() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val end = calendar.timeInMillis
        loadLaporan(start, end)
    }

    private fun loadBulanIni() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val start = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = calendar.timeInMillis
        loadLaporan(start, end)
    }

    private fun loadTahunIni() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        val start = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
        val end = calendar.timeInMillis
        loadLaporan(start, end)
    }

    private fun loadLaporan(startDate: Long, endDate: Long) {
        progressBar.visibility = View.VISIBLE
        rvLaporan.visibility = View.GONE
        layoutEmpty.visibility = View.GONE

        // 1. TOTAL PRODUK + TOTAL NILAI STOK (Dari node "menu")
        dbMenu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalP = 0
                var totalN = 0L
                listMenu.clear()

                for (snap in snapshot.children) {
                    val menu = snap.getValue(ModelMenu::class.java)
                    menu?.let {
                        totalP++
                        totalN += (it.stok * it.hargaModal)
                        listMenu.add(it)
                    }
                }

                tvTotalProduk.text = totalP.toString()
                tvTotalNilai.text = rupiah(totalN)
                
                adapterLaporan.notifyDataSetChanged()
                
                if (listMenu.isEmpty()) {
                    layoutEmpty.visibility = View.VISIBLE
                    rvLaporan.visibility = View.GONE
                } else {
                    layoutEmpty.visibility = View.GONE
                    rvLaporan.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LaporanActivity, "Gagal memuat data menu", Toast.LENGTH_SHORT).show()
            }
        })

        // 2. TOTAL KEUNTUNGAN (Dari node "penjualan" yang diisi oleh TransaksiActivity)
        dbPenjualan.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalK = 0L

                for (snap in snapshot.children) {
                    val penjualan = snap.getValue(ModelPenjualan::class.java)
                    penjualan?.let {
                        val tgl = it.tanggal ?: 0
                        if (tgl in startDate..endDate) {
                            totalK += it.keuntungan ?: 0
                        }
                    }
                }

                tvTotalKeuntungan.text = rupiah(totalK)
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@LaporanActivity, "Gagal memuat data penjualan", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun rupiah(number: Long): String {
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        return formatRupiah.format(number).replace(",00", "")
    }

    // ADAPTER INTERNAL UNTUK LAPORAN STOK
    class LaporanProdukAdapter(private val list: List<ModelMenu>) :
        RecyclerView.Adapter<LaporanProdukAdapter.ViewHolder>() {

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val ivFoto: ImageView = v.findViewById(R.id.ivItemFoto)
            val tvNama: TextView = v.findViewById(R.id.tvItemNama)
            val tvHarga: TextView = v.findViewById(R.id.tvItemHarga)
            val tvStok: TextView = v.findViewById(R.id.tvItemCabang)
            val btnEdit: View = v.findViewById(R.id.btnEditMenu)
            val btnDelete: View = v.findViewById(R.id.btnDeleteMenu)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvNama.text = item.namaProduk
            holder.tvHarga.text = "Modal: Rp ${item.hargaModal}"
            holder.tvStok.text = "Stok: ${item.stok}"
            
            holder.btnEdit.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE

            Glide.with(holder.itemView.context)
                .load(item.urlFoto)
                .placeholder(R.drawable.ic_produk)
                .into(holder.ivFoto)
        }

        override fun getItemCount() = list.size
    }
}
