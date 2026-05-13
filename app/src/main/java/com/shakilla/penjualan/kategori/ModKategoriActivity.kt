package com.shakilla.penjualan.kategori

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.FirebaseDatabase
import com.shakilla.penjualan.R
import com.shakilla.penjualan.model.ModelKategori

class ModKategoriActivity : AppCompatActivity() {

    private lateinit var tvJudul: TextView
    private lateinit var etNamaKategori: EditText
    private lateinit var cgStatus: ChipGroup // Tambahkan ChipGroup
    private lateinit var btnSimpan: MaterialButton
    private lateinit var btnBack: ImageView

    // Firebase
    private val database = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val myRef = database.getReference("kategori")

    private var kategoriId: String? = null
    private var kategoriNama: String? = null
    private var kategoriStatus: String? = null

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_NAMA = "extra_nama"
        const val EXTRA_STATUS = "extra_status"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        getIntentData()
        setupClickListeners()
    }

    private fun init() {
        tvJudul = findViewById(R.id.tvJudul)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        cgStatus = findViewById(R.id.cgStatus) // Inisialisasi ChipGroup
        btnSimpan = findViewById(R.id.btnSimpan)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun getIntentData() {
        val nama = etNamaKategori.text.toString().trim()
        if (nama.isEmpty()) {
            etNamaKategori.error = "Wajib diisi"
            return
        }

        // Mengambil status dari Chip: jika Aktif terpilih kirim "1", selain itu "0"
        val statusSimpan = if (cgStatus.checkedChipId == R.id.chipAktif) "1" else "0"

        val idInput = kategoriId ?: myRef.push().key ?: ""
        val data = ModelKategori(idInput, nama, statusSimpan)

        myRef.child(idInput).setValue(data).addOnSuccessListener {
            Toast.makeText(this, "Berhasil!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            simpanData()
        }
    }

    private fun simpanData() {
        val nama = etNamaKategori.text.toString().trim()

        // 1. Validasi Input
        if (nama.isEmpty()) {
            etNamaKategori.error = "Nama kategori tidak boleh kosong"
            return
        }

        // 2. Ambil status dari Chip yang dipilih
        // Jika chipAktif yang dipilih, simpan "1", selain itu "0"
        val statusSimpan = if (cgStatus.checkedChipId == R.id.chipAktif) "1" else "0"

        // 3. Tentukan ID (Baru atau Edit)
        val idInput = kategoriId ?: myRef.push().key ?: ""

        // 4. Susun Objek
        val data = ModelKategori(
            idKategori = idInput,
            namaKategori = nama,
            statusKategori = statusSimpan
        )

        // 5. Proses Simpan ke Firebase
        myRef.child(idInput).setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("FIREBASE", "Error: ", e)
            }
    }
}