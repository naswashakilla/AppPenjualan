package com.shakilla.penjualan

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.shakilla.penjualan.menu.DataMenuActivity
import com.shakilla.penjualan.kategori.DataKategoriActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hubungkan Variabel dengan ID di XML
        val btnProduk: LinearLayout = findViewById(R.id.btnMenuProduk)
        val btnKategori: LinearLayout = findViewById(R.id.btnMenuKategori)

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
    }
}