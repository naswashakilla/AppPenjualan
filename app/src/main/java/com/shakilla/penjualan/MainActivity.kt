package com.shakilla.penjualan

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.shakilla.penjualan.kategori.DataMenuActivity
import com.shakilla.penjualan.kategori.DataKategoriActivity
import com.shakilla.penjualan.model.ModelPenjualan
import com.shakilla.penjualan.pegawai.DataPegawaiActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvEstimasi: TextView
    private lateinit var tvTanggal: TextView
    private lateinit var tvSapaan: TextView
    private lateinit var btnMenuTop: android.widget.ImageView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvEstimasi = findViewById(R.id.tvEstimasi)
        tvTanggal = findViewById(R.id.tvtanggal)
        tvSapaan = findViewById(R.id.tvSapaan)
        btnMenuTop = findViewById(R.id.btnMenuTop)

        ambilDataUser()
        loadEstimasiHariIni()

        btnMenuTop.setOnClickListener { showMenuHub() }

        val ivProfile = findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivProfileTop)
        ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        val btnProduk: CardView = findViewById(R.id.btnMenuProduk)
        val btnKategori:  CardView = findViewById(R.id.btnMenuKategori)
        val btnPegawai: CardView = findViewById(R.id.btnMenuPegawai)
        val btnPelanggan: LinearLayout = findViewById(R.id.btnMenuPelanggan)
        val btnTransaksi: LinearLayout = findViewById(R.id.btnMenuTransaksi)
        val btnPrinter: CardView = findViewById(R.id.btnMenuPrinter)
        val btnCabang: CardView = findViewById(R.id.btnMenuCabang)
        val btnLayanan: CardView = findViewById(R.id.btnMenuLayanan)
        val btnLaporan: LinearLayout = findViewById(R.id.btnMenuLaporan)

        btnProduk.setOnClickListener {
            val intent = Intent(this, DataMenuActivity::class.java)
            startActivity(intent)
        }

        btnKategori.setOnClickListener {
            val intent = Intent(this, DataKategoriActivity::class.java)
            startActivity(intent)
        }

        btnPegawai.setOnClickListener {
            startActivity(Intent(this, DataPegawaiActivity::class.java))
        }

        btnPelanggan.setOnClickListener {
            startActivity(Intent(this, PelangganActivity::class.java))
        }

        btnTransaksi.setOnClickListener {
            startActivity(Intent(this,TransaksiActivity::class.java))
        }

        btnPrinter.setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }

        btnCabang.setOnClickListener {
            startActivity(Intent(this, CabangActivity::class.java))
        }

        btnLayanan.setOnClickListener {
            startActivity(Intent(this, LayananActivity::class.java))
        }

        btnLaporan.setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }
    }

    private fun ambilDataUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val userRef = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(uid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val nama = snapshot.child("nama").getValue(String::class.java)
                    val sapaan = getSapaanWaktu()
                    tvSapaan.text = "$sapaan, $nama!"
                }
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadEstimasiHariIni() {
        // Format Tanggal
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        tvTanggal.text = sdf.format(Date())

        // Range Waktu Hari Ini
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis

        val dbPenjualan = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("penjualan")

        dbPenjualan.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalHariIni = 0L
                for (item in snapshot.children) {
                    val penjualan = item.getValue(ModelPenjualan::class.java)
                    if (penjualan != null) {
                        val tgl = penjualan.tanggal ?: 0
                        if (tgl in start..end) {
                            totalHariIni += penjualan.total ?: 0L
                        }
                    }
                }
                tvEstimasi.text = formatRupiah(totalHariIni)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun formatRupiah(amount: Long): String {
        val localeID = Locale("id", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        return formatRupiah.format(amount).replace(",00", "")
    }

    private fun getSapaanWaktu(): String {
        val jam = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (jam) {
            in 4..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }

    private fun showMenuHub() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_menu_hub, null)

        view.findViewById<LinearLayout>(R.id.menuSetStruk).setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this, "Fitur Pengaturan Toko segera hadir!", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<LinearLayout>(R.id.menuEkspor).setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this, "Menyiapkan file Excel...", Toast.LENGTH_SHORT).show()
            // Logika ekspor data bisa ditambahkan di sini
        }

        view.findViewById<LinearLayout>(R.id.menuTentang).setOnClickListener {
            dialog.dismiss()
            AlertDialog.Builder(this)
                .setTitle("Tentang Aplikasi")
                .setMessage("App Penjualan v1.0\nDikembangkan oleh Shakilla\nSistem Manajemen Kasir & Stok Modern")
                .setPositiveButton("Oke", null)
                .show()
        }

        view.findViewById<LinearLayout>(R.id.menuLogout).setOnClickListener {
            dialog.dismiss()
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        dialog.setContentView(view)
        dialog.show()
    }
}
