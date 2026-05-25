package com.shakilla.penjualan.kategori

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.shakilla.penjualan.R
import com.shakilla.penjualan.model.ModelCabang
import com.shakilla.penjualan.model.ModelMenu

class ModMenuActivity : AppCompatActivity() {

    private lateinit var etNama: TextInputEditText
    private lateinit var etHarga: TextInputEditText
    private lateinit var etHargaModal: TextInputEditText
    private lateinit var etStok: TextInputEditText
    private lateinit var etUrlFoto: TextInputEditText
    private lateinit var ivPreview: ShapeableImageView
    private lateinit var actvKategori: AutoCompleteTextView
    private lateinit var cgStatus: ChipGroup
    private lateinit var btnSimpan: MaterialButton
    private lateinit var btnPilihFoto: MaterialButton
    private lateinit var etPilihCabang: TextInputEditText
    private lateinit var tvJudul: TextView
    
    private var idMenuEdit: String? = null
    private var fotoUri: Uri? = null
    private var originalPadding: Int = 0

    private val semuaCabang = mutableListOf<String>()
    private val cabangTerpilih = mutableListOf<String>()
    private lateinit var selectedArray: BooleanArray

    private val database = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val myRef = database.getReference("menu")
    private val databaseKategori = database.getReference("kategori")
    private val databaseCabang = database.getReference("cabang")
    private val storageRef = FirebaseStorage.getInstance().reference.child("foto_menu")
    private val daftarKategori = ArrayList<String>()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            fotoUri = it
            ivPreview.setPadding(0, 0, 0, 0)
            ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(this).load(it).into(ivPreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_menu)

        init()

        val dataEdit = intent.getSerializableExtra("MENU_DATA") as? ModelMenu

        if (dataEdit != null) {
            idMenuEdit = dataEdit.idMenu
            tvJudul.text = "Edit Menu"
            btnSimpan.text = "Update Menu"

            etNama.setText(dataEdit.namaProduk)
            etHarga.setText(dataEdit.harga.toString())
            etHargaModal.setText(dataEdit.hargaModal.toString())
            etStok.setText(dataEdit.stok.toString())
            etUrlFoto.setText(dataEdit.urlFoto)
            
            if (!dataEdit.urlFoto.isNullOrBlank()) {
                ivPreview.setPadding(0, 0, 0, 0)
                ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(this).load(dataEdit.urlFoto).placeholder(R.drawable.ic_camera).into(ivPreview)
            }
            
            actvKategori.setText(dataEdit.kategori)
            cabangTerpilih.addAll(dataEdit.listCabang ?: emptyList())
            etPilihCabang.setText(cabangTerpilih.joinToString(", "))

            btnSimpan.setOnClickListener {
                prosesSimpan(idMenuEdit!!)
            }
        } else {
            tvJudul.text = "Tambah Menu"
            btnSimpan.setOnClickListener { 
                val newId = myRef.push().key ?: ""
                prosesSimpan(newId) 
            }
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

        btnPilihFoto.setOnClickListener { pickImage.launch("image/*") }
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun init() {
        etNama = findViewById(R.id.etNamaProduk)
        etHarga = findViewById(R.id.etHarga)
        etHargaModal = findViewById(R.id.etHargaModal)
        etStok = findViewById(R.id.etStok)
        etUrlFoto = findViewById(R.id.etUrlFoto)
        etPilihCabang = findViewById(R.id.actvCabang)
        actvKategori = findViewById(R.id.actvKategori)
        cgStatus = findViewById(R.id.cgStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnPilihFoto = findViewById(R.id.btnPilihFotoMenu)
        tvJudul = findViewById(R.id.tvJudul)
        ivPreview = findViewById(R.id.ivPreviewFoto)

        // Simpan padding awal (ikon kamera)
        originalPadding = ivPreview.paddingLeft

        actvKategori.setOnClickListener {
            actvKategori.showDropDown()
        }

        etUrlFoto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (fotoUri == null) {
                    val url = s.toString().trim()
                    if (url.isNotEmpty()) {
                        ivPreview.setPadding(0, 0, 0, 0)
                        ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
                        Glide.with(this@ModMenuActivity)
                            .load(url)
                            .placeholder(R.drawable.ic_camera)
                            .error(R.drawable.ic_camera)
                            .into(ivPreview)
                    } else {
                        ivPreview.setPadding(originalPadding, originalPadding, originalPadding, originalPadding)
                        ivPreview.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        ivPreview.setImageResource(R.drawable.ic_camera)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun prosesSimpan(id: String) {
        val nama = etNama.text.toString().trim()
        val harga = etHarga.text.toString().toLongOrNull() ?: 0L
        val hargaModal = etHargaModal.text.toString().toLongOrNull() ?: 0L
        val stok = etStok.text.toString().toIntOrNull() ?: 0
        val urlManual = etUrlFoto.text.toString().trim()
        val kategori = actvKategori.text.toString()
        val status = if (findViewById<Chip>(R.id.chipAktif).isChecked) "1" else "0"

        if (nama.isEmpty() || harga <= 0L || cabangTerpilih.isEmpty()) {
            Toast.makeText(this, "Nama, Harga, dan Cabang wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Sedang menyimpan...", Toast.LENGTH_SHORT).show()

        if (fotoUri != null) {
            val ref = storageRef.child("$id.jpg")
            ref.putFile(fotoUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    simpanKeDatabase(id, nama, harga, hargaModal, stok, kategori, status, downloadUri.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Gagal upload: ${it.message}", Toast.LENGTH_LONG).show()
                // Tetap simpan data menggunakan URL manual jika ada
                simpanKeDatabase(id, nama, harga, hargaModal, stok, kategori, status, urlManual)
            }
        } else {
            simpanKeDatabase(id, nama, harga, hargaModal, stok, kategori, status, urlManual)
        }
    }

    private fun simpanKeDatabase(id: String, nama: String, harga: Long, modal: Long, stok: Int, kat: String, stat: String, url: String) {
        val menu = ModelMenu(id, nama, harga, modal, stok, kat, stat, url, cabangTerpilih)
        myRef.child(id).setValue(menu).addOnSuccessListener {
            Toast.makeText(this, "Menu berhasil disimpan!", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal simpan database: ${it.message}", Toast.LENGTH_SHORT).show()
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
}
