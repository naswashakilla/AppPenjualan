package com.shakilla.penjualan.kategori

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.shakilla.penjualan.R
import com.shakilla.penjualan.model.ModelCabang
import com.shakilla.penjualan.model.ModelMenu

class ModMenuActivity : AppCompatActivity() {

    private lateinit var etNama: TextInputEditText
    private lateinit var etHarga: TextInputEditText
    private lateinit var etStok: TextInputEditText
    private lateinit var etUrlFoto: TextInputEditText
    private lateinit var actvKategori: AutoCompleteTextView
    private lateinit var cgStatus: ChipGroup
    private lateinit var btnSimpan: MaterialButton
    private lateinit var etPilihCabang: TextInputEditText
    private lateinit var tvJudul: TextView
    private var idMenuEdit: String? = null


    private val semuaCabang = mutableListOf<String>()
    private val cabangTerpilih = mutableListOf<String>()
    private lateinit var selectedArray: BooleanArray

    private val database = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val myRef = database.getReference("menu")
    private val databaseKategori = database.getReference("kategori")
    private val databaseCabang = database.getReference("cabang")
    private val daftarKategori = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_menu)

        init()

        val dataEdit = intent.getSerializableExtra("MENU_DATA") as? ModelMenu

        if (dataEdit != null) {
            tvJudul.text = "Edit Menu"
            btnSimpan.text = "Update Menu"

            etNama.setText(dataEdit.namaProduk)
            etHarga.setText(dataEdit.harga.toString())
            etStok.setText(dataEdit.stok.toString())
            etUrlFoto.setText(dataEdit.urlFoto)
            actvKategori.setText(dataEdit.kategori)
            cabangTerpilih.addAll(dataEdit.listCabang ?: emptyList())
            etPilihCabang.setText(cabangTerpilih.joinToString(", "))
            tvJudul.text = "Edit Menu"
            btnSimpan.text = "Update Menu"

            btnSimpan.setOnClickListener {
                updateData(dataEdit.idMenu!!)
            }
        }else {
            tvJudul.text = "Tambah Menu"
            btnSimpan.setOnClickListener { simpanData() }
        }

        ambilDataKategori()
        loadDataCabangDariFirebase()

        etPilihCabang.setOnClickListener {
            if (semuaCabang.isNotEmpty()) {
                showMultiSelectDialog()
            } else {
                Toast.makeText(this, "Data cabang belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun init() {
        etNama = findViewById(R.id.etNamaProduk)
        etHarga = findViewById(R.id.etHarga)
        etStok = findViewById(R.id.etStok)
        etUrlFoto = findViewById(R.id.etUrlFoto)
        etPilihCabang = findViewById(R.id.actvCabang) // Pastikan ID di XML adalah actvCabang
        actvKategori = findViewById(R.id.actvKategori)
        cgStatus = findViewById(R.id.cgStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
        tvJudul = findViewById(R.id.tvJudul)

        actvKategori.setOnClickListener {
            actvKategori.showDropDown()
        }
    }

    private fun ambilDataKategori() {
        databaseKategori.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                daftarKategori.clear()
                for (item in snapshot.children) {
                    val namaKategori = item.child("namaKategori").getValue(String::class.java)
                    if (namaKategori != null) daftarKategori.add(namaKategori)
                }
                val adapter = ArrayAdapter(this@ModMenuActivity, android.R.layout.simple_dropdown_item_1line, daftarKategori)
                actvKategori.setAdapter(adapter)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadDataCabangDariFirebase() {
        databaseCabang.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                semuaCabang.clear()
                for (data in snapshot.children) {
                    val item = data.getValue(ModelCabang::class.java)
                    item?.namaCabang?.let { semuaCabang.add(it) }
                }
                selectedArray = BooleanArray(semuaCabang.size)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showMultiSelectDialog() {
        val items = semuaCabang.toTypedArray()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih Cabang (Bisa lebih dari 1)")
        builder.setMultiChoiceItems(items, selectedArray) { _, which, isChecked ->
            selectedArray[which] = isChecked
        }

        builder.setPositiveButton("Selesai") { _, _ ->
            cabangTerpilih.clear()
            for (i in items.indices) {
                if (selectedArray[i]) {
                    cabangTerpilih.add(items[i])
                }
            }
            etPilihCabang.setText(cabangTerpilih.joinToString(", "))
        }

        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun updateData(id: String) {
        val nama = etNama.text.toString().trim()
        val harga = etHarga.text.toString().toLongOrNull() ?: 0L
        val stok = etStok.text.toString().toIntOrNull() ?: 0
        val url = etUrlFoto.text.toString().trim()
        val kategori = actvKategori.text.toString()
        val status = if (findViewById<Chip>(R.id.chipAktif).isChecked) "1" else "0"

        if (nama.isEmpty() || harga <= 0L || cabangTerpilih.isEmpty()) {
            Toast.makeText(this, "Semua data wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val menuUpdate = ModelMenu(id, nama, harga, stok, kategori, status, url, cabangTerpilih)

        myRef.child(id).setValue(menuUpdate).addOnSuccessListener {
            Toast.makeText(this, "Menu Berhasil Diperbarui!", Toast.LENGTH_SHORT).show()
            finish() // Kembali ke halaman daftar menu
        }
    }

    private fun simpanData() {
        val nama = etNama.text.toString().trim()
        val hargaText = etHarga.text.toString()
        val stokText = etStok.text.toString()
        val url = etUrlFoto.text.toString().trim()
        val kategori = actvKategori.text.toString()

        val harga = hargaText.toLongOrNull() ?: 0L
        val stok = stokText.toIntOrNull() ?: 0
        val status = if (findViewById<Chip>(R.id.chipAktif).isChecked) "1" else "0"

        if (nama.isEmpty() || harga <= 0L || cabangTerpilih.isEmpty()) {
            Toast.makeText(this, "Nama, Harga, dan Cabang wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val id = myRef.push().key ?: ""
        // Simpan 'cabangTerpilih' (ArrayList) ke dalam ModelMenu
        val menu = ModelMenu(
            idMenu = id,
            namaProduk = nama,
            harga = harga,
            stok = stok,
            kategori = kategori,
            status = status,
            urlFoto = url,
            listCabang = cabangTerpilih
        )

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