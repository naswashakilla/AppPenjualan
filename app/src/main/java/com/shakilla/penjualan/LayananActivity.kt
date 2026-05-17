package com.shakilla.penjualan

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class LayananActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layanan)

        val btnBack = findViewById<ImageButton>(R.id.btnBackLayanan)
        val btnPesanan = findViewById<MaterialCardView>(R.id.btnPesananTransaksi)
        val btnServis = findViewById<MaterialCardView>(R.id.btnKlaimServis)
        val btnKomunikasi = findViewById<MaterialCardView>(R.id.btnKomunikasi)

        btnBack.setOnClickListener { finish() }

        btnPesanan.setOnClickListener {
            startActivity(Intent(this, TransaksiActivity::class.java))
        }

        btnServis.setOnClickListener {
            startActivity(Intent(this, KlaimServisActivity::class.java))
        }

        btnKomunikasi.setOnClickListener {
            startActivity(Intent(this, KomunikasiActivity::class.java))
        }
    }
}
