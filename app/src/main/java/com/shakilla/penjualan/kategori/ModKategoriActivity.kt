package com.shakilla.penjualan.kategori

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.shakilla.penjualan.R
import com.shakilla.penjualan.model.ModelKategori
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase

class ModKategoriActivity : AppCompatActivity() {

    // Deklarasi variabel dengan tipe yang jelas
    private lateinit var tvJudul: TextView
    private lateinit var etNamaKategori: EditText
    private lateinit var chipStatus: Chip
    private lateinit var autoCompleteStatus: AutoCompleteTextView
    private lateinit var btnSimpan: MaterialButton
    private lateinit var btnBack: ImageView

    // Firebase
    private val database = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val myRef = database.getReference("kategori")

    // Variabel untuk menampung data yang akan diedit
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

        // Perbaikan: Gunakan root view yang benar, misalnya ConstraintLayout utama
        // val rootView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.modKategori)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        getIntentData()
        setupSpinner()
        setupClickListeners()
        updateTitle()
    }

    private fun init() {
        // Inisialisasi semua view dengan ID yang benar dari XML
        //tvJudul = findViewById(R.id.tvJudul)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        //spStatusKategori = findViewById(R.id.spStatusKategori)

        // Perbaikan: Ambil AutoCompleteTextView dari dalam TextInputLayout
        //autoCompleteStatus = findViewById(R.id.autoCompleteStatus)

        btnSimpan = findViewById(R.id.btnSimpan)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun getIntentData() {
        // Ambil data dari intent jika ada (mode edit)
        kategoriId = intent.getStringExtra(EXTRA_ID)
        kategoriNama = intent.getStringExtra(EXTRA_NAMA)
        kategoriStatus = intent.getStringExtra(EXTRA_STATUS)

        // Jika mode edit, tampilkan data yang ada
        if (kategoriId != null) {
            etNamaKategori.setText(kategoriNama)
        }
    }

    private fun updateTitle() {
        // Perbaikan: Gunakan tvJudul (bukan tvJuduI)
        //tvJudul.text = if (kategoriId == null) "Tambah Kategori" else "Edit Kategori"
    }

    private fun setupSpinner() {
        // Data untuk spinner
        val listStatus = arrayOf("Aktif", "Tidak Aktif")

        // Buat adapter dengan tipe yang jelas
        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            listStatus
        )

        // Set adapter ke AutoCompleteTextView
        //autoCompleteStatus.setAdapter(adapter)

        // Set item yang dipilih jika mode edit
        if (kategoriStatus != null) {
            //autoCompleteStatus.setText(kategoriStatus, false)
        }
    }

    private fun setupClickListeners() {
        // Tombol back
        btnBack.setOnClickListener {
            finish()
        }

        // Tombol simpan
        btnSimpan.setOnClickListener {
            //Toast.makeText(this, "Simpan diklik", Toast.LENGTH_SHORT).show()
            simpanData()
        }
    }

    private fun simpanData() {
        val nama = etNamaKategori.text.toString().trim()

        // 1. Validasi Input (Penting!)
        if (nama.isEmpty()) {
            etNamaKategori.error = "Nama kategori tidak boleh kosong"
            return
        }

        // 2. Tentukan apakah ini Mode Tambah atau Mode Edit
        val idInput = kategoriId ?: myRef.push().key ?: ""

        // 3. Susun Objek (Sesuaikan parameter dengan ModelKategori kamu)
        val data = ModelKategori(
            idKategori = idInput,
            namaKategori = nama,
            statusKategori = "1" // Default Aktif
        )

        // 4. Proses Simpan
        myRef.child(idInput).setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()
                finish() // Tutup halaman
            }
            .addOnFailureListener { e ->
                // Ini akan memberitahu jika ada masalah koneksi/permission
                Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                android.util.Log.e("FIREBASE", "Error: ", e)
            }
    }

//        if (status.isEmpty()) {
//            autoCompleteStatus.error = "Status harus dipilih"
//            autoCompleteStatus.requestFocus()
//            return
//        }

//        if (kategoriId == null) {
//            // Mode TAMBAH data baru
//            val id = myRef.push().key
//
//            if (id == null) {
//                Toast.makeText(this, "Gagal membuat ID", Toast.LENGTH_SHORT).show()
//                return
//            }
//
//            // Buat objek kategori
//            val kategori = ModelKategori(
//                idKategori = id,
//                namaKategori = nama,
//                statusKategori = "1"
//            )
//
//            // Simpan ke Firebase
//            myRef.child(id).setValue(kategori)
//                .addOnSuccessListener {
//                    Toast.makeText(this, "Data kategori berhasil disimpan", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//        } else {
//            // Mode EDIT data yang sudah ada
//            val kategori = ModelKategori(
//                idKategori = kategoriId!!,
//                namaKategori = nama,
//                statusKategori = "1"
//            )
//
//            // Update data di Firebase
//            myRef.child(kategoriId!!).setValue(kategori)
//                .addOnSuccessListener {
//                    Toast.makeText(this, "Data kategori berhasil diupdate", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(this, "Gagal mengupdate data: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//        }
    }