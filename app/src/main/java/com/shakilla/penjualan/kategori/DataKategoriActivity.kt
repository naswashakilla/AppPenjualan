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
// ... import lainnya
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class DataKategoriActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var listKategori: ArrayList<ModelKategori>
    private lateinit var adapter: KategoriAdapter
    private lateinit var database: DatabaseReference
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_kategori)

        init()
        setupSearch()
        loadData()

        fabAdd.setOnClickListener {
            startActivity(Intent(this, ModKategoriActivity::class.java))
        }
    }

    private fun init() {
        recyclerView = findViewById(R.id.rvKategori)
        fabAdd = findViewById(R.id.fabAdd)
        etSearch = findViewById(R.id.etSearchKategori)

        recyclerView.layoutManager = LinearLayoutManager(this)
        listKategori = ArrayList()

        adapter = KategoriAdapter(
            listKategori, // INI YANG PENTING
            onEdit = { kategori ->
                val intent = Intent(this, ModKategoriActivity::class.java)
                intent.putExtra("DATA_KATEGORI", kategori)
                startActivity(intent)
            },
            onHapus = { kategori ->
                showDeleteDialog(kategori)
            }
        )
        recyclerView.adapter = adapter

        database = FirebaseDatabase
            .getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("kategori")
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString()) // Jalankan filter saat mengetik
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listKategori.clear()
                for (dataSnapshot in snapshot.children) {
                    val kategori = dataSnapshot.getValue(ModelKategori::class.java)
                    kategori?.let { listKategori.add(it) }
                }
                adapter.updateData(ArrayList(listKategori))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataKategoriActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteDialog(kategori: ModelKategori) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Kategori")
            .setMessage("Yakin ingin menghapus kategori ${kategori.namaKategori}?")
            .setPositiveButton("Hapus") { _, _ ->
                FirebaseDatabase.getInstance().getReference("kategori")
                    .child(kategori.idKategori!!).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Kategori dihapus", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}