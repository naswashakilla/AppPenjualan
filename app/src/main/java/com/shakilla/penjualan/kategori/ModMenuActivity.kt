package com.shakilla.penjualan.kategori

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.shakilla.penjualan.R
import com.shakilla.penjualan.model.ModelMenu

class ModMenuActivity : AppCompatActivity() {

    private lateinit var etNama: TextInputEditText
    private lateinit var etHarga: TextInputEditText
    private lateinit var etStok: TextInputEditText
    private lateinit var etUrlFoto: TextInputEditText
    private lateinit var actvCabang: AutoCompleteTextView
    private lateinit var cgStatus: ChipGroup
    private lateinit var btnSimpan: MaterialButton

    // Inisialisasi Database (Sesuai URL database kamu di image_649742.jpg)
    private val database = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val myRef = database.getReference("menu")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_menu)

        init()
        setupDropdownCabang()

        btnSimpan.setOnClickListener { simpanData() }
    }

    private fun init() {
        etNama = findViewById(R.id.etNamaProduk)
        etHarga = findViewById(R.id.etHarga)
        etStok = findViewById(R.id.etStok)
        etUrlFoto = findViewById(R.id.etUrlFoto) // Input URL manual
        actvCabang = findViewById(R.id.actvCabang)
        cgStatus = findViewById(R.id.cgStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun setupDropdownCabang() {
        val cabangList = arrayOf("Cabang Pusat", "Cabang Barat", "Cabang Timur")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cabangList)
        actvCabang.setAdapter(adapter)
    }

    private fun simpanData() {
        val nama = etNama.text.toString().trim()
        val harga = etHarga.text.toString().trim()
        val stok = etStok.text.toString().trim()
        val url = etUrlFoto.text.toString().trim()
        val cabang = actvCabang.text.toString()

        // Ambil status dari Chip
        val status = if (cgStatus.checkedChipId == R.id.chipAktif) "1" else "0"

        if (nama.isEmpty() || harga.isEmpty()) {
            Toast.makeText(this, "Nama dan Harga wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        // Langsung simpan ke Database tanpa upload foto
        val id = myRef.push().key ?: ""
        val menu = ModelMenu(id, nama, harga, stok, cabang, status, url)

        myRef.child(id).setValue(menu)
            .addOnSuccessListener {
                Toast.makeText(this, "Menu berhasil disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}