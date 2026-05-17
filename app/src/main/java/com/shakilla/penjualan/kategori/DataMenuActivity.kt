package com.shakilla.penjualan.kategori

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.shakilla.penjualan.R
import com.shakilla.penjualan.model.ModelMenu

class DataMenuActivity : AppCompatActivity() {

    private lateinit var rvMenu: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var etSearch: TextInputEditText // Tambahkan variabel untuk Search Bar
    private lateinit var adapter: MenuAdapter
    private val listMenu = mutableListOf<ModelMenu>()

    // Pastikan URL Database sesuai dengan milikmu
    private val dbRef = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("menu")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_menu)

        initView()
        setupRecyclerView()
        setupSearch()
        ambilDataFirebase()

        fabAdd.setOnClickListener {
            startActivity(Intent(this, ModMenuActivity::class.java))
        }
    }

    private fun initView() {
        rvMenu = findViewById(R.id.rvMenu)
        fabAdd = findViewById(R.id.fabAddMenu)
        etSearch = findViewById(R.id.etSearchMenu)
    }

    private fun setupRecyclerView() {
        adapter = MenuAdapter(listMenu,
            onEdit = { menu ->
                // Pindah ke ModMenuActivity sambil membawa data menu yang mau diedit
                val intent = Intent(this, ModMenuActivity::class.java)
                intent.putExtra("MENU_DATA", menu)
                startActivity(intent)
            },
            onHapus = { menu ->
                showDeleteDialog(menu)
            }
        )
        rvMenu.layoutManager = LinearLayoutManager(this)
        rvMenu.adapter = adapter
    }

    private fun setupSearch() {
        // Listener untuk mendeteksi setiap ketikan di kolom pencarian
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Panggil fungsi filter yang ada di MenuAdapter
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun ambilDataFirebase() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listMenu.clear()
                for (dataSnapshot in snapshot.children) {
                    val menu = dataSnapshot.getValue(ModelMenu::class.java)
                    menu?.let { listMenu.add(it) }
                }
                // Gunakan updateData dari adapter agar listFull juga terisi
                adapter.updateData(listMenu)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataMenuActivity, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteDialog(menu: ModelMenu) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Hapus Menu")
        builder.setMessage("Apakah anda yakin ingin menghapus ${menu.namaProduk}?")
        builder.setPositiveButton("Hapus") { _, _ ->
            // Eksekusi hapus di Firebase berdasarkan ID
            FirebaseDatabase.getInstance().getReference("menu")
                .child(menu.idMenu!!).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }}