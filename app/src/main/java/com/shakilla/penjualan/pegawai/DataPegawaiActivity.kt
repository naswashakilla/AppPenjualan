package com.shakilla.penjualan.pegawai

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.shakilla.penjualan.Pegawai
import com.shakilla.penjualan.R

class DataPegawaiActivity : AppCompatActivity() {

    private lateinit var adapter: PegawaiAdapter
    // Gunakan listPegawai yang ini untuk menampung data asli dari Firebase
    private val listPegawaiMaster = mutableListOf<Pegawai>()

    private val dbRef = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("pegawai")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_pegawai)

        // 1. Inisialisasi RecyclerView & Adapter DULU
        setupRecyclerView()

        // 2. Pasang Listener Search SETELAH Adapter siap
        setupSearch()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<FloatingActionButton>(R.id.fabTambah).setOnClickListener {
            startActivity(Intent(this, TambahPegawaiActivity::class.java))
        }

        listenData()
    }

    private fun setupRecyclerView() {
        // Inisialisasi dengan list kosong di awal
        adapter = PegawaiAdapter(mutableListOf(),
            onEdit = { pegawai ->
                val intent = Intent(this, TambahPegawaiActivity::class.java).apply {
                    putExtra("mode", "edit")
                    putExtra("id", pegawai.id)
                    putExtra("nama", pegawai.nama)
                    putExtra("email", pegawai.email)
                    putExtra("telp", pegawai.telp)
                    putExtra("jabatan", pegawai.jabatan)
                    putExtra("gaji", pegawai.gaji)
                    putExtra("fotoUrl", pegawai.fotoUrl)
                }
                startActivity(intent)
            },
            onHapus = { pegawai ->
                AlertDialog.Builder(this)
                    .setTitle("Hapus Pegawai")
                    .setMessage("Apakah Anda yakin ingin menghapus ${pegawai.nama}?")
                    .setPositiveButton("Ya") { _, _ ->
                        dbRef.child(pegawai.id).removeValue().addOnSuccessListener {
                            android.widget.Toast.makeText(this, "Pegawai berhasil dihapus", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Tidak", null)
                    .show()
            }
        )

        findViewById<RecyclerView>(R.id.rvPegawai).apply {
            layoutManager = LinearLayoutManager(this@DataPegawaiActivity)
            adapter = this@DataPegawaiActivity.adapter
        }
    }

    private fun listenData() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listPegawaiMaster.clear()
                for (data in snapshot.children) {
                    val p = data.getValue(Pegawai::class.java)
                    if (p != null) listPegawaiMaster.add(p)
                }

                // 3. Kirim data ke adapter menggunakan fungsi updateData
                adapter.updateData(listPegawaiMaster)

                val isEmpty = listPegawaiMaster.isEmpty()
                findViewById<LinearLayout>(R.id.layoutEmpty).visibility =
                    if (isEmpty) View.VISIBLE else View.GONE
                findViewById<TextView>(R.id.tvJumlahPegawai).text =
                    "${listPegawaiMaster.size} orang"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupSearch() {
        val etSearch = findViewById<EditText>(R.id.etSearchPegawai)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Memanggil fungsi filter di adapter
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
