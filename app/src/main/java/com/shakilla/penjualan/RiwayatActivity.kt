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
        var htmlContent = """
            <html>
            <head>
                <style>
                    body { font-family: 'Courier New', Courier, monospace; width: 100%; margin: 0; padding: 10px; font-size: 12pt; color: black; background: white; }
                    .header { text-align: center; margin-bottom: 10px; }
                    .footer { text-align: center; margin-top: 20px; font-size: 10pt; }
                    .divider { border-top: 1px dashed black; margin: 5px 0; }
                    table { width: 100%; border-collapse: collapse; }
                    .total { font-weight: bold; font-size: 14pt; }
                    .item-name { padding: 2px 0; }
                    .item-price { text-align: right; padding: 2px 0; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h2 style="margin: 0;">TOKOKU</h2>
                    <p style="margin: 2px 0;">(SALINAN STRUK)</p>
                </div>
                <div class="divider"></div>
                <p style="margin: 5px 0;">Tgl: ${data.tanggal}</p>
                <p style="margin: 5px 0;">Kasir: ${data.kasir ?: "-"}</p>
                <p style="margin: 5px 0;">ID: ${data.id?.takeLast(8)}</p>
                <div class="divider"></div>
                <table>
        """.trimIndent()

        data.items?.forEach { item ->
            val nama = item["namaProduk"] ?: "-"
            val jumlah = item["jumlah"] ?: 0
            val subtotal = (item["subtotal"] as? Number)?.toLong() ?: 0L
            
            htmlContent += """
                <tr>
                    <td class="item-name">$nama<br><small>$jumlah x ...</small></td>
                    <td class="item-price">Rp $subtotal</td>
                </tr>
            """.trimIndent()
        }

        htmlContent += """
                </table>
                <div class="divider"></div>
                <table>
                    <tr class="total">
                        <td>TOTAL</td>
                        <td style="text-align: right;">Rp ${data.total}</td>
                    </tr>
                    <tr>
                        <td>BAYAR</td>
                        <td style="text-align: right;">Rp ${data.bayar}</td>
                    </tr>
                    <tr>
                        <td>KEMBALI</td>
                        <td style="text-align: right;">Rp ${data.kembalian}</td>
                    </tr>
                </table>
                <div class="divider"></div>
                <div class="footer">
                    <p>Terima Kasih Atas Kunjungan Anda</p>
                </div>
            </body>
            </html>
        """.trimIndent()

        val webView = WebView(this)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
                val jobName = "Salinan_Struk_${data.id?.takeLast(4)}"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(jobName, printAdapter, PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.JPN_YOU4)
                    .build())
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