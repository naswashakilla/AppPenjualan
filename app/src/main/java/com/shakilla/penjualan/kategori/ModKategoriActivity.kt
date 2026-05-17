package com.shakilla.penjualan.kategori

import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.FirebaseDatabase
import com.shakilla.penjualan.R
import com.shakilla.penjualan.model.ModelKategori

class ModKategoriActivity : AppCompatActivity() {

    private lateinit var tvJudul: TextView
    private lateinit var etNamaKategori: EditText
    private lateinit var cgStatus: ChipGroup
    private lateinit var btnSimpan: MaterialButton
    private lateinit var btnBack: ImageView

    private val database = FirebaseDatabase
        .getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("kategori")

    private var dataEdit: ModelKategori? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)

        init()

        // Ambil data dari intent
        dataEdit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("DATA_KATEGORI", ModelKategori::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("DATA_KATEGORI")
        }

        // Jika mode edit
        dataEdit?.let { kategori ->

            tvJudul.text = "Edit Kategori"
            btnSimpan.text = "Update Kategori"

            etNamaKategori.setText(kategori.namaKategori)

            // Set status chip
            if (kategori.statusKategori == "1") {
                cgStatus.check(R.id.chipAktif)
            } else {
                cgStatus.check(R.id.chipNonAktif)
            }
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnSimpan.setOnClickListener {
            simpanAtauUpdate()
        }
    }

    private fun init() {
        tvJudul = findViewById(R.id.tvJudul)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        cgStatus = findViewById(R.id.cgStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun simpanAtauUpdate() {

        val nama = etNamaKategori.text.toString().trim()

        if (nama.isEmpty()) {
            etNamaKategori.error = "Nama kategori wajib diisi"
            return
        }

        val status = if (cgStatus.checkedChipId == R.id.chipAktif) {
            "1"
        } else {
            "0"
        }

        // Jika edit → pakai id lama
        // Jika tambah → buat id baru
        val idKategori = dataEdit?.idKategori ?: database.push().key!!

        val kategori = ModelKategori(
            idKategori = idKategori,
            namaKategori = nama,
            statusKategori = status
        )

        database.child(idKategori)
            .setValue(kategori)
            .addOnSuccessListener {

                if (dataEdit != null) {
                    Toast.makeText(this, "Kategori berhasil diupdate", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }

                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
            }
    }
}