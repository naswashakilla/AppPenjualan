package com.shakilla.penjualan.kategori


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shakilla.penjualan.R
import com.shakilla.penjualan.model.ModelKategori

// ... (import tetap sama)

class DataKategori : AppCompatActivity() {

    private lateinit var fabData: FloatingActionButton
    private lateinit var rvKategori: RecyclerView
    private lateinit var listKategori: ArrayList<ModelKategori>
    private lateinit var adapter: AdapterKategori

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_kategori)

        // Pastikan R.id.main benar-benar ada di activity_data_kategori.xml
        val rootView = findViewById<android.view.View>(R.id.main)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Inisialisasi Views
        fabData = findViewById(R.id.fabTambahKategori)
        rvKategori = findViewById(R.id.rvData_Kategori)

        // Setup RecyclerView
        listKategori = ArrayList()
        adapter = AdapterKategori(listKategori)
        rvKategori.layoutManager = LinearLayoutManager(this)
        rvKategori.adapter = adapter

        fabData.setOnClickListener {
            val intent = Intent(this, ModKategori::class.java)
            startActivity(intent)
        }  

        loadData()
    }

    private fun loadData() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listKategori.clear()
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val kategori = data.getValue(ModelKategori::class.java)
                        if (kategori != null) {
                            listKategori.add(kategori)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataKategori, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}