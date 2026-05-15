package com.shakilla.penjualan

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.shakilla.penjualan.kategori.DataMenuActivity
import com.shakilla.penjualan.kategori.DataKategoriActivity
import com.shakilla.penjualan.pegawai.DataPegawaiActivity

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ambilDataUser()

        val ivProfile = findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivProfileTop)
        ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        val btnProduk: CardView = findViewById(R.id.btnMenuProduk)
        val btnKategori:  CardView = findViewById(R.id.btnMenuKategori)
        val btnPegawai: CardView = findViewById(R.id.btnMenuPegawai)

        // Logika Klik Tombol Produk
        btnProduk.setOnClickListener {
            val intent = Intent(this, DataMenuActivity::class.java)
            startActivity(intent)
        }

        // Logika Klik Tombol Kategori
        btnKategori.setOnClickListener {
            val intent = Intent(this, DataKategoriActivity::class.java)
            startActivity(intent)
        }

        btnPegawai.setOnClickListener {
            startActivity(Intent(this, DataPegawaiActivity::class.java))
        }
    }

    private fun ambilDataUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val userRef = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(uid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val nama = snapshot.child("nama").getValue(String::class.java)
                    findViewById<TextView>(R.id.tvSapaan).text = "Selamat Pagi, $nama!"
                }
            }
        } else {
            // Jika tidak ada user, paksa kembali ke Login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun getSapaanWaktu(): String {
        val jam = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (jam) {
            in 4..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }
}