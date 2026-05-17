package com.shakilla.penjualan

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.shakilla.penjualan.model.ModelPenjualan
import com.shakilla.penjualan.model.ModelProduk
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

    // FIREBASE
    private val dbProduk = FirebaseDatabase
        .getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("produk")

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

        rvLaporan = findViewById(R.id.rv_laporan_produk)
        rvLaporan.layoutManager = LinearLayoutManager(this)
    }

    // DROPDOWN PERIODE
    private fun setupPeriode() {

        val listPeriode = listOf(
            "Hari Ini",
            "Minggu Ini",
            "Bulan Ini",
            "Tahun Ini"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            listPeriode
        )

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

    // FILTER HARI INI
    private fun loadHariIni() {

        val calendar = Calendar.getInstance()

        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        loadLaporan(start, end)
    }

    // =========================================================
    // FILTER MINGGU INI
    // =========================================================

    private fun loadMingguIni() {

        val calendar = Calendar.getInstance()

        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

        val start = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_WEEK, 6)

        val end = calendar.timeInMillis

        loadLaporan(start, end)
    }

    // FILTER BULAN INI
    private fun loadBulanIni() {

        val calendar = Calendar.getInstance()

        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val start = calendar.timeInMillis

        calendar.set(
            Calendar.DAY_OF_MONTH,
            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        )

        val end = calendar.timeInMillis

        loadLaporan(start, end)
    }

    // FILTER TAHUN INI
    private fun loadTahunIni() {

        val calendar = Calendar.getInstance()

        calendar.set(Calendar.DAY_OF_YEAR, 1)

        val start = calendar.timeInMillis

        calendar.set(
            Calendar.DAY_OF_YEAR,
            calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
        )

        val end = calendar.timeInMillis

        loadLaporan(start, end)
    }

    // =========================================================
    // LOAD SEMUA LAPORAN
    // =========================================================

    private fun loadLaporan(
        startDate: Long,
        endDate: Long
    ) {

        progressBar.visibility = View.VISIBLE

        // TOTAL PRODUK + TOTAL NILAI STOK
        dbProduk.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                var totalProduk = 0
                var totalNilaiStok = 0

                for (snap in snapshot.children) {

                    val produk =
                        snap.getValue(ModelProduk::class.java)

                    produk?.let {

                        totalProduk++

                        val stok = it.stokProduk ?: 0
                        val modal = it.hargaModal ?: 0

                        totalNilaiStok += stok * modal
                    }
                }

                tvTotalProduk.text = totalProduk.toString()

                tvTotalNilai.text =
                    rupiah(totalNilaiStok)
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(
                    this@LaporanActivity,
                    "Gagal memuat produk",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // TOTAL KEUNTUNGAN
        dbPenjualan.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                var totalKeuntungan = 0

                for (snap in snapshot.children) {

                    val penjualan =
                        snap.getValue(ModelPenjualan::class.java)

                    penjualan?.let {

                        val tanggal = it.tanggal ?: 0

                        if (tanggal in startDate..endDate) {

                            totalKeuntungan +=
                                it.keuntungan ?: 0
                        }
                    }
                }

                tvTotalKeuntungan.text =
                    rupiah(totalKeuntungan)

                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {

                progressBar.visibility = View.GONE

                Toast.makeText(
                    this@LaporanActivity,
                    "Gagal memuat penjualan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun rupiah(number: Int): String {

        val localeID = Locale("in", "ID")

        val formatRupiah =
            NumberFormat.getCurrencyInstance(localeID)

        return formatRupiah.format(number)
    }
}