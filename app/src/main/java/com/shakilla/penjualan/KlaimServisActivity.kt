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
import com.shakilla.penjualan.model.ModelServis
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class KlaimServisActivity : AppCompatActivity() {

    private lateinit var rvServis: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var btnBack: ImageButton

    private val database = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val servisRef = database.getReference("servis")

    private val listServis = mutableListOf<ModelServis>()
    private lateinit var adapter: ServisAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_klaim_servis)

        rvServis = findViewById(R.id.rvServis)
        fabTambah = findViewById(R.id.fabTambahServis)
        btnBack = findViewById(R.id.btnBackServis)

        adapter = ServisAdapter(listServis,
            onEdit = { servis -> showDialogInput(servis) },
            onDelete = { servis -> hapusServis(servis) }
        )

        rvServis.layoutManager = LinearLayoutManager(this)
        rvServis.adapter = adapter

        btnBack.setOnClickListener { finish() }
        fabTambah.setOnClickListener { showDialogInput(null) }

        loadDataServis()
    }

    private fun loadDataServis() {
        servisRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listServis.clear()
                for (data in snapshot.children) {
                    val s = data.getValue(ModelServis::class.java)
                    if (s != null) listServis.add(s)
                }
                listServis.reverse()
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showDialogInput(servis: ModelServis?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_input_servis, null)
        val etNama = view.findViewById<TextInputEditText>(R.id.etNamaPelangganServisDlg)
        val etBarang = view.findViewById<TextInputEditText>(R.id.etNamaBarangDlg)
        val etKerusakan = view.findViewById<TextInputEditText>(R.id.etKerusakanDlg)
        val etBiaya = view.findViewById<TextInputEditText>(R.id.etBiayaDlg)
        val spnStatus = view.findViewById<Spinner>(R.id.spnStatusServis)

        val statusList = arrayOf("Proses", "Selesai", "Diambil")
        val spnAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusList)
        spnStatus.adapter = spnAdapter

        if (servis != null) {
            etNama.setText(servis.namaPelanggan)
            etBarang.setText(servis.namaBarang)
            etKerusakan.setText(servis.deskripsiKerusakan)
            etBiaya.setText(servis.biaya.toString())
            spnStatus.setSelection(statusList.indexOf(servis.status))
        }

        AlertDialog.Builder(this)
            .setTitle(if (servis == null) "Tambah Pengajuan Servis" else "Edit Data Servis")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString().trim()
                val barang = etBarang.text.toString().trim()
                val kerusakan = etKerusakan.text.toString().trim()
                val biaya = etBiaya.text.toString().toLongOrNull() ?: 0L
                val status = spnStatus.selectedItem.toString()

                if (nama.isEmpty() || barang.isEmpty()) {
                    Toast.makeText(this, "Nama dan Barang wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val id = servis?.idServis ?: servisRef.push().key ?: ""
                val data = ModelServis(id, nama, barang, kerusakan, biaya, status, System.currentTimeMillis())

                servisRef.child(id).setValue(data).addOnSuccessListener {
                    Toast.makeText(this, "Berhasil disimpan", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusServis(servis: ModelServis) {
        AlertDialog.Builder(this)
            .setTitle("Hapus")
            .setMessage("Yakin ingin menghapus data servis ini?")
            .setPositiveButton("Ya") { _, _ ->
                servis.idServis?.let {
                    servisRef.child(it).removeValue()
                }
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun formatRupiah(amount: Long): String {
        val localeID = Locale("id", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        return formatRupiah.format(amount).replace(",00", "")
    }

    // ADAPTER INTERNAL
    inner class ServisAdapter(
        private val list: List<ModelServis>,
        private val onEdit: (ModelServis) -> Unit,
        private val onDelete: (ModelServis) -> Unit
    ) : RecyclerView.Adapter<ServisAdapter.ViewHolder>() {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvNama: TextView = v.findViewById(R.id.tvNamaPelangganServis)
            val tvTgl: TextView = v.findViewById(R.id.tvTglServis)
            val tvStatus: TextView = v.findViewById(R.id.tvStatusServis)
            val tvBarang: TextView = v.findViewById(R.id.tvNamaBarang)
            val tvKerusakan: TextView = v.findViewById(R.id.tvDeskripsiKerusakan)
            val tvBiaya: TextView = v.findViewById(R.id.tvBiayaServis)
            val btnEdit: ImageView = v.findViewById(R.id.btnEditServis)
            val btnDel: ImageView = v.findViewById(R.id.btnDeleteServis)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_servis, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val s = list[position]
            holder.tvNama.text = s.namaPelanggan
            holder.tvBarang.text = s.namaBarang
            holder.tvKerusakan.text = s.deskripsiKerusakan ?: "-"
            holder.tvBiaya.text = "Biaya: ${formatRupiah(s.biaya)}"
            holder.tvStatus.text = s.status

            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            holder.tvTgl.text = sdf.format(Date(s.tanggalMasuk ?: 0))

            // Warna Status
            when(s.status) {
                "Selesai" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_status_aktif)
                "Diambil" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_item)
                else -> holder.tvStatus.setBackgroundResource(R.drawable.bg_status_nonaktif)
            }

            holder.btnEdit.setOnClickListener { onEdit(s) }
            holder.btnDel.setOnClickListener { onDelete(s) }
        }

        override fun getItemCount() = list.size
    }
}
