package com.shakilla.penjualan.menu

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.shakilla.penjualan.R
import com.shakilla.penjualan.kategori.ModMenuActivity
import com.shakilla.penjualan.model.ModelMenu

class DataMenuActivity : AppCompatActivity() {

    private lateinit var rvMenu: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var adapter: MenuAdapter
    private val listMenu = mutableListOf<ModelMenu>()

    private val dbRef = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("menu")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_menu) // Gunakan layout list yang sudah ada

        rvMenu = findViewById(R.id.rvMenu) // Gunakan ID recycler view kamu
        fabAdd = findViewById(R.id.fabAddMenu)

        setupRecyclerView()
        ambilDataFirebase()

        fabAdd.setOnClickListener {
            startActivity(Intent(this, ModMenuActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = MenuAdapter(listMenu)
        rvMenu.layoutManager = LinearLayoutManager(this)
        rvMenu.adapter = adapter
    }

    private fun ambilDataFirebase() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listMenu.clear()
                for (dataSnapshot in snapshot.children) {
                    val menu = dataSnapshot.getValue(ModelMenu::class.java)
                    menu?.let { listMenu.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataMenuActivity, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}