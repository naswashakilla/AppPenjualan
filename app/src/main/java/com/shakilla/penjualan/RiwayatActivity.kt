package com.shakilla.penjualan

import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shakilla.penjualan.ModelTransaksi
import java.util.Locale

class RiwayatActivity : AppCompatActivity() {
    private lateinit var rvRiwayat: RecyclerView
    private lateinit var btnBack: ImageButton

    // Gunakan URL database yang sama dengan TransaksiActivity
    private val database = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val transaksiRef = database.getReference("transaksi")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        // Inisialisasi View
        rvRiwayat = findViewById(R.id.rvRiwayat)
        btnBack = findViewById(R.id.btnBackRiwayat)

        rvRiwayat.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener { finish() }

        loadRiwayat()
    }

    private fun loadRiwayat() {
        transaksiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listRiwayat = ArrayList<ModelTransaksi>()

                if (snapshot.exists()) {
                    for (data in snapshot.children) { // Perbaikan: gunakan 'in' bukan 'dalam'
                        val transaksi = data.getValue(ModelTransaksi::class.java)
                        if (transaksi != null) {
                            listRiwayat.add(transaksi)
                        }
                    }

                    // Urutkan dari transaksi terbaru (paling atas)
                    listRiwayat.reverse()

                    // Inisialisasi adapter dengan callback untuk tombol cetak dan hapus
                    val adapter = RiwayatAdapter(listRiwayat, 
                        onPrintClick = { transaksi ->
                            cetakUlangStruk(transaksi)
                        },
                        onDeleteClick = { transaksi ->
                            hapusRiwayat(transaksi)
                        }
                    )
                    rvRiwayat.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RiwayatActivity, "Gagal memuat: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fungsi cetak ulang khusus untuk data dari ModelTransaksi
    private fun cetakUlangStruk(data: ModelTransaksi) {
        var htmlContent = "<html><body style='font-family:monospace;'>" +
                "<h2 style='text-align:center;'>STRUK PENJUALAN</h2>" +
                "<p>Tanggal: ${data.tanggal}</p><hr><table style='width:100%'>"

        data.items?.forEach { item ->
            htmlContent += "<tr>" +
                    "<td>${item["namaProduk"]} x${item["jumlah"]}</td>" +
                    "<td style='text-align:right;'>${item["subtotal"]}</td>" +
                    "</tr>"
        }

        htmlContent += "</table><hr>" +
                "<h3 style='text-align:right;'>TOTAL: Rp ${data.total}</h3>" +
                "<p style='text-align:center;'>Cetak Ulang Riwayat</p></body></html>"

        val webView = WebView(this)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Struk_${data.id}")
                printManager.print("Struk Penjualan", printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }

    private fun hapusRiwayat(transaksi: ModelTransaksi) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Riwayat")
            .setMessage("Apakah Anda yakin ingin menghapus riwayat transaksi ini?")
            .setPositiveButton("Hapus") { _, _ ->
                val id = transaksi.id
                if (id != null) {
                    // Hapus dari node transaksi
                    transaksiRef.child(id).removeValue()
                        .addOnSuccessListener {
                            // Juga hapus dari node penjualan (laporan)
                            database.getReference("penjualan").child(id).removeValue()
                            Toast.makeText(this, "Riwayat berhasil dihapus", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal menghapus: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}