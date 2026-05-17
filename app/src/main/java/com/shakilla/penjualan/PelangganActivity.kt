package com.shakilla.penjualan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.shakilla.penjualan.model.ModelPelanggan
import java.text.SimpleDateFormat
import java.util.*

class PelangganActivity : AppCompatActivity() {

    private lateinit var rvPelanggan: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var btnBack: ImageButton
    
    private val database = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val pelangganRef = database.getReference("pelanggan")
    
    private val listPelanggan = mutableListOf<ModelPelanggan>()
    private lateinit var adapter: PelangganAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pelanggan)

        rvPelanggan = findViewById(R.id.rvPelanggan)
        fabTambah = findViewById(R.id.fabTambahPelanggan)
        btnBack = findViewById(R.id.btnBackPelanggan)

        adapter = PelangganAdapter(listPelanggan, 
            onEdit = { pelanggan -> showDialogInput(pelanggan) },
            onDelete = { pelanggan -> hapusPelanggan(pelanggan) }
        )

        rvPelanggan.layoutManager = LinearLayoutManager(this)
        rvPelanggan.adapter = adapter

        btnBack.setOnClickListener { finish() }
        fabTambah.setOnClickListener { showDialogInput(null) }

        loadDataPelanggan()
    }

    private fun loadDataPelanggan() {
        pelangganRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listPelanggan.clear()
                for (data in snapshot.children) {
                    val p = data.getValue(ModelPelanggan::class.java)
                    if (p != null) listPelanggan.add(p)
                }
                listPelanggan.reverse()
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showDialogInput(pelanggan: ModelPelanggan?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_input_pelanggan, null)
        val etNama = view.findViewById<TextInputEditText>(R.id.etNamaPelangganDlg)
        val etPreferensi = view.findViewById<TextInputEditText>(R.id.etPreferensiDlg)
        val etKeluhan = view.findViewById<TextInputEditText>(R.id.etKeluhanDlg)
        val etFeedback = view.findViewById<TextInputEditText>(R.id.etFeedbackDlg)

        if (pelanggan != null) {
            etNama.setText(pelanggan.namaPelanggan)
            etPreferensi.setText(pelanggan.preferensi)
            etKeluhan.setText(pelanggan.keluhan)
            etFeedback.setText(pelanggan.feedback)
        }

        AlertDialog.Builder(this)
            .setTitle(if (pelanggan == null) "Tambah Catatan" else "Edit Catatan")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString().trim()
                val pref = etPreferensi.text.toString().trim()
                val keluh = etKeluhan.text.toString().trim()
                val feed = etFeedback.text.toString().trim()

                if (nama.isEmpty()) {
                    Toast.makeText(this, "Nama wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val id = pelanggan?.idPelanggan ?: pelangganRef.push().key ?: ""
                val data = ModelPelanggan(id, nama, pref, keluh, feed, System.currentTimeMillis())

                pelangganRef.child(id).setValue(data).addOnSuccessListener {
                    Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusPelanggan(pelanggan: ModelPelanggan) {
        AlertDialog.Builder(this)
            .setTitle("Hapus")
            .setMessage("Yakin ingin menghapus catatan ini?")
            .setPositiveButton("Ya") { _, _ ->
                pelanggan.idPelanggan?.let {
                    pelangganRef.child(it).removeValue()
                }
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    // ADAPTER INTERNAL
    inner class PelangganAdapter(
        private val list: List<ModelPelanggan>,
        private val onEdit: (ModelPelanggan) -> Unit,
        private val onDelete: (ModelPelanggan) -> Unit
    ) : RecyclerView.Adapter<PelangganAdapter.ViewHolder>() {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvNama: TextView = v.findViewById(R.id.tvNamaPelanggan)
            val tvTgl: TextView = v.findViewById(R.id.tvTglCatatan)
            val tvPref: TextView = v.findViewById(R.id.tvPreferensi)
            val tvKeluh: TextView = v.findViewById(R.id.tvKeluhan)
            val tvFeed: TextView = v.findViewById(R.id.tvFeedback)
            val btnEdit: ImageView = v.findViewById(R.id.btnEditPelanggan)
            val btnDel: ImageView = v.findViewById(R.id.btnDeletePelanggan)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_pelanggan, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val p = list[position]
            holder.tvNama.text = p.namaPelanggan
            holder.tvPref.text = p.preferensi ?: "-"
            holder.tvKeluh.text = p.keluhan ?: "-"
            holder.tvFeed.text = p.feedback ?: "-"

            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            holder.tvTgl.text = sdf.format(Date(p.tanggalCatatan ?: 0))

            holder.btnEdit.setOnClickListener { onEdit(p) }
            holder.btnDel.setOnClickListener { onDelete(p) }
        }

        override fun getItemCount() = list.size
    }
}
