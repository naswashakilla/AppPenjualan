package com.shakilla.penjualan

import android.content.Intent
import android.net.Uri
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
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.shakilla.penjualan.model.ModelPelanggan
import com.shakilla.penjualan.model.ModelRating
import java.text.SimpleDateFormat
import java.util.*

class KomunikasiActivity : AppCompatActivity() {

    private lateinit var rvRating: RecyclerView
    private lateinit var rvPromo: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var layoutRating: View
    private lateinit var layoutPromo: View
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var etPesanPromo: TextInputEditText

    private val database = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val ratingRef = database.getReference("rating")
    private val pelangganRef = database.getReference("pelanggan")

    private val listRating = mutableListOf<ModelRating>()
    private val listPelanggan = mutableListOf<ModelPelanggan>()
    private lateinit var ratingAdapter: RatingAdapter
    private lateinit var promoAdapter: PromoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_komunikasi)

        initViews()
        setupTabs()
        loadData()
    }

    private fun initViews() {
        rvRating = findViewById(R.id.rvRating)
        rvPromo = findViewById(R.id.rvPelangganPromo)
        tabLayout = findViewById(R.id.tabKomunikasi)
        layoutRating = findViewById(R.id.layoutRating)
        layoutPromo = findViewById(R.id.layoutPromo)
        fabTambah = findViewById(R.id.fabTambahRating)
        etPesanPromo = findViewById(R.id.etPesanPromo)

        findViewById<ImageButton>(R.id.btnBackKomunikasi).setOnClickListener { finish() }

        ratingAdapter = RatingAdapter(listRating)
        rvRating.layoutManager = LinearLayoutManager(this)
        rvRating.adapter = ratingAdapter

        promoAdapter = PromoAdapter(listPelanggan) { pelanggan ->
            kirimWA(pelanggan)
        }
        rvPromo.layoutManager = LinearLayoutManager(this)
        rvPromo.adapter = promoAdapter

        fabTambah.setOnClickListener { showDialogRating() }
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    layoutRating.visibility = View.VISIBLE
                    layoutPromo.visibility = View.GONE
                    fabTambah.visibility = View.VISIBLE
                } else {
                    layoutRating.visibility = View.GONE
                    layoutPromo.visibility = View.VISIBLE
                    fabTambah.visibility = View.GONE
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadData() {
        // Load Ratings
        ratingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listRating.clear()
                for (data in snapshot.children) {
                    data.getValue(ModelRating::class.java)?.let { listRating.add(it) }
                }
                listRating.reverse()
                ratingAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Load Pelanggan for Promo
        pelangganRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listPelanggan.clear()
                for (data in snapshot.children) {
                    data.getValue(ModelPelanggan::class.java)?.let { listPelanggan.add(it) }
                }
                promoAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showDialogRating() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_input_rating, null)
        val etNama = view.findViewById<TextInputEditText>(R.id.etNamaPelangganRatingDlg)
        val rb = view.findViewById<RatingBar>(R.id.rbRatingDlg)
        val etUlasan = view.findViewById<TextInputEditText>(R.id.etUlasanDlg)

        AlertDialog.Builder(this)
            .setTitle("Tambah Rating & Ulasan")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString().trim()
                val ulasan = etUlasan.text.toString().trim()
                val rating = rb.rating

                if (nama.isEmpty()) {
                    Toast.makeText(this, "Nama wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val id = ratingRef.push().key ?: ""
                val data = ModelRating(id, nama, rating, ulasan, System.currentTimeMillis())
                ratingRef.child(id).setValue(data)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun kirimWA(pelanggan: ModelPelanggan) {
        val pesan = etPesanPromo.text.toString().trim()
        if (pesan.isEmpty()) {
            Toast.makeText(this, "Tulis pesan promo terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://api.whatsapp.com/send?text=${Uri.encode(pesan)}"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    // ADAPTER RATING
    inner class RatingAdapter(private val list: List<ModelRating>) : 
        RecyclerView.Adapter<RatingAdapter.ViewHolder>() {
        
        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvNama: TextView = v.findViewById(R.id.tvNamaPelangganRating)
            val tvUlasan: TextView = v.findViewById(R.id.tvUlasanRating)
            val tvTgl: TextView = v.findViewById(R.id.tvTglRating)
            val rb: RatingBar = v.findViewById(R.id.rbItemRating)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = 
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_rating, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val r = list[position]
            holder.tvNama.text = r.namaPelanggan
            holder.tvUlasan.text = r.ulasan ?: "-"
            holder.rb.rating = r.rating
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            holder.tvTgl.text = sdf.format(Date(r.tanggal ?: 0))
        }
        override fun getItemCount() = list.size
    }

    // ADAPTER PROMO
    inner class PromoAdapter(private val list: List<ModelPelanggan>, val onSend: (ModelPelanggan) -> Unit) : 
        RecyclerView.Adapter<PromoAdapter.ViewHolder>() {
        
        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvNama: TextView = v.findViewById(R.id.tvNamaPelangganPromo)
            val tvPref: TextView = v.findViewById(R.id.tvPreferensiPromo)
            val btnKirim: Button = v.findViewById(R.id.btnKirimWA)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = 
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_promo, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val p = list[position]
            holder.tvNama.text = p.namaPelanggan
            holder.tvPref.text = p.preferensi ?: "-"
            holder.btnKirim.setOnClickListener { onSend(p) }
        }
        override fun getItemCount() = list.size
    }
}
