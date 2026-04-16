package com.shakilla.penjualan.kategori

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shakilla.penjualan.R
import com.shakilla.penjualan.model.ModelKategori
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.shakilla.penjualan.viewmodel.DataKategoriViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class DataKategoriActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var listKategori: ArrayList<ModelKategori>
    private lateinit var adapter: KategoriAdapter
    private lateinit var database: DatabaseReference
    private lateinit var fabAdd: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_kategori)

        init()
        loadData()

        fabAdd.setOnClickListener {
            val intent = Intent(this, ModKategoriActivity::class.java)
            startActivity(intent)
        }
    }

    private fun init() {
        recyclerView = findViewById(R.id.rvData_Kategori)
        fabAdd = findViewById(R.id.fabAdd)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        listKategori = ArrayList()

        // PERBAIKAN: Tambahkan onItemClick di sini
        adapter = KategoriAdapter(
            listKategori = listKategori,
            onItemClick = { kategori ->
                // Aksi saat item kategori diklik
                Toast.makeText(this, "${kategori.namaKategori}", Toast.LENGTH_SHORT).show()

                // Contoh: Buka detail kategori
                // val intent = Intent(this, DetailKategoriActivity::class.java)
                // intent.putExtra("id_kategori", kategori.idKategori)
                // startActivity(intent)
            }
        )

        recyclerView.adapter = adapter
        database = FirebaseDatabase
            .getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("kategori")
    }

    private fun loadData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listKategori.clear()

                for (dataSnapshot in snapshot.children) {
                    val kategori = dataSnapshot.getValue(ModelKategori::class.java)
                    if (kategori != null) {
                        listKategori.add(kategori)
                        android.util.Log.d("DATA_FIREBASE", "Data: ${kategori.namaKategori}")
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataKategoriActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}