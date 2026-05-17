package com.shakilla.penjualan

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.shakilla.penjualan.model.ModelCabang

class CabangActivity : AppCompatActivity() {

    private lateinit var rvCabang: RecyclerView
    private lateinit var etNama: TextInputEditText
    private lateinit var etKet: TextInputEditText
    private lateinit var btnSimpan: MaterialButton

    private val dbCabang = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("cabang")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cabang)

        // Inisialisasi View
        rvCabang = findViewById(R.id.rvCabang)
        etNama = findViewById(R.id.etNamaCabang)
        etKet = findViewById(R.id.etKeteranganCabang)
        btnSimpan = findViewById(R.id.btnSimpanCabang)

        rvCabang.layoutManager = LinearLayoutManager(this)

        findViewById<android.widget.ImageButton>(R.id.btnBackCabang).setOnClickListener {
            finish()
        }

        btnSimpan.setOnClickListener { simpanKeFirebase() }

        loadDaftarCabang()
    }

    private fun simpanKeFirebase() {
        val nama = etNama.text.toString().trim()
        val ket = etKet.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Nama cabang wajib diisi"
            return
        }

        val id = dbCabang.push().key ?: ""
        val data = ModelCabang(id, nama, ket)

        dbCabang.child(id).setValue(data).addOnSuccessListener {
            etNama.text?.clear()
            etKet.text?.clear()
            Toast.makeText(this, "Cabang berhasil ditambahkan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDaftarCabang() {
        dbCabang.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ModelCabang>()
                for (data in snapshot.children) {
                    val item = data.getValue(ModelCabang::class.java)
                    if (item != null) list.add(item)
                }

                // Set Adapter ke RecyclerView
                rvCabang.adapter = CabangAdapter(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CabangActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}