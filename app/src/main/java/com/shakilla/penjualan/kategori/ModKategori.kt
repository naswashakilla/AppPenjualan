package com.shakilla.penjualan.kategori

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.shakilla.penjualan.R
import com.shakilla.penjualan.model.ModelKategori

class ModKategori : AppCompatActivity() {

    // 1. Gunakan 'lateinit' untuk view dan 'by lazy' untuk referensi Firebase
    // Ini adalah praktik yang lebih modern dan aman.
    private val myRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("kategori")
    }

    private lateinit var etNamaKategori: EditText
    private lateinit var spStatusKategori: Spinner
    private lateinit var btnSimpan: Button
    private lateinit var tvJudul: TextView // Deklarasi tvJudul

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() tidak lagi diperlukan dengan dependensi terbaru
        setContentView(R.layout.activity_mod_kategori)

        // 2. Panggil initViews() untuk menginisialisasi semua view
        initViews()

        // Menangani window insets untuk layout utama
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.modKategori)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 3. Pindahkan logika setup ke fungsi terpisah agar onCreate lebih rapi
        setupSpinner()
        setupClickListener()
    }

    private fun initViews() {
        tvJudul = findViewById(R.id.tvJudul)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        spStatusKategori = findViewById(R.id.spStatusKategori)
        btnSimpan = findViewById(R.id.btnSimpan)

        // Mengatur judul aktivitas
        tvJudul.text = "Tambah Kategori"
    }

    private fun setupSpinner() {
        val statusList = resources.getStringArray(R.array.status_array)
        val adapterStatus = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusList)
        spStatusKategori.setAdapter(adapterStatus)

        // Atur nilai default jika daftar tidak kosong
        if (statusList.isNotEmpty()) {
            //spStatusKategori.setText(statusList[0], false)
        }
    }

    private fun setupClickListener() {
        btnSimpan.setOnClickListener {
            // Menonaktifkan tombol untuk mencegah klik ganda
            btnSimpan.isEnabled = false
            saveKategori()
        }
    }

    private fun saveKategori() {
        val namaKategori = etNamaKategori.text.toString().trim()
//        val statusKategori = spStatusKategori.text.toString()
        val statusKategori = spStatusKategori.selectedItem.toString()

        if (namaKategori.isEmpty()) {
            etNamaKategori.error = "Nama Kategori Tidak Boleh Kosong"
            etNamaKategori.requestFocus()
            btnSimpan.isEnabled = true // Aktifkan kembali tombol jika validasi gagal
            return
        }

        // Membuat ID unik untuk data baru
        val key = myRef.push().key
        if (key == null) {
            Toast.makeText(this, "Gagal mendapatkan key database", Toast.LENGTH_SHORT).show()
            btnSimpan.isEnabled = true // Aktifkan kembali tombol
            return
        }

        val kategoriData = ModelKategori(
            idKategori = key,
            namaKategori = namaKategori,
            statusKategori = statusKategori
        )

        myRef.child(key).setValue(kategoriData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                // 4. [PERBAIKAN UTAMA] Tutup aktivitas ini setelah berhasil
                finish()
            }
            .addOnFailureListener { exception ->
                // Tampilkan pesan error yang lebih deskriptif
                Toast.makeText(this, "Data Gagal Disimpan: ${exception.message}", Toast.LENGTH_LONG).show()
                btnSimpan.isEnabled = true // Aktifkan kembali tombol jika gagal
            }
    }

    // Fungsi init() yang lama tidak lagi diperlukan karena sudah diganti dengan initViews()
    // dan pemanggilan langsung di onCreate.
}
