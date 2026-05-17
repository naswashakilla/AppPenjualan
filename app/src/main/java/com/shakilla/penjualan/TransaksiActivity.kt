package com.shakilla.penjualan

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.shakilla.penjualan.model.ModelMenu
import com.shakilla.penjualan.R
import java.text.NumberFormat
import java.util.Locale
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient

class TransaksiActivity : AppCompatActivity() {

    // Views
    private lateinit var rvMenu: RecyclerView
    private lateinit var rvPesanan: RecyclerView
    private lateinit var layoutKategoriTab: LinearLayout
    private lateinit var tvSubtotal: TextView
    private lateinit var tvDiskon: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvJumlahItem: TextView
    private lateinit var btnBayar: MaterialButton
    private lateinit var btnHapusSemua: MaterialButton
    private lateinit var btnRiwayat: ImageButton
    private lateinit var pbLoading: ProgressBar
    private lateinit var btnCetak: Button

    private lateinit var menuAdapter: MenuTransaksiAdapter
    private lateinit var pesananAdapter: PesananAdapter

    private val semuaMenu     = mutableListOf<ModelMenu>()
    private val menuTampil    = mutableListOf<ModelMenu>()
    private val listPesanan   = mutableListOf<ItemPesanan>()
    private val kategoriList  = mutableListOf<String>()
    private var kategoriAktif = "Semua"
    private var diskon: Long  = 0L
    private var totalBayar: Long = 0

    // Firebase — URL sama dengan ModMenuActivity
    private val database = FirebaseDatabase.getInstance(
        "https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private val menuRef = database.getReference("menu")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaksi)

        initViews()
        setupRvMenu()
        setupRvPesanan()
        setupButtons()
        loadMenuDariFirebase()
    }

    private fun initViews() {
        rvMenu           = findViewById(R.id.rvMenu)
        rvPesanan        = findViewById(R.id.rvPesanan)
        layoutKategoriTab = findViewById(R.id.layoutKategoriTab)
        tvSubtotal       = findViewById(R.id.tvSubtotal)
        tvDiskon         = findViewById(R.id.tvDiskon)
        tvTotal          = findViewById(R.id.tvTotal)
        tvJumlahItem     = findViewById(R.id.tvJumlahItem)
        btnBayar         = findViewById(R.id.btnBayar)
        btnHapusSemua    = findViewById(R.id.btnHapusSemua)
        btnRiwayat       = findViewById(R.id.btnRiwayat)
        pbLoading        = findViewById(R.id.pbLoading)
        btnCetak         = findViewById(R.id.btnCetak)

        val etSearch = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { filterMenu() }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadMenuDariFirebase() {
        pbLoading.visibility = View.VISIBLE

        menuRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                semuaMenu.clear()

                for (item in snapshot.children) {
                    val menu = item.getValue(ModelMenu::class.java)
                    // Hanya tampilkan menu yang statusnya "1" (Aktif)
                    if (menu != null && menu.status == "1") {
                        semuaMenu.add(menu)
                    }
                }

                pbLoading.visibility = View.GONE

                kategoriList.clear()
                kategoriList.add("Semua")
                kategoriList.addAll(
                    semuaMenu.mapNotNull { it.kategori }
                        .filter { it.isNotBlank() }
                        .distinct()
                )

                menuTampil.clear()
                menuTampil.addAll(semuaMenu)
                menuAdapter.notifyDataSetChanged()
                setupKategoriTab()
            }

            override fun onCancelled(error: DatabaseError) {
                pbLoading.visibility = View.GONE
                Toast.makeText(
                    this@TransaksiActivity,
                    "Gagal memuat menu: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupKategoriTab() {
        layoutKategoriTab.removeAllViews()

        kategoriList.forEach { kategori ->
            val tab = LayoutInflater.from(this)
                .inflate(R.layout.item_kategori_tab, layoutKategoriTab, false) as TextView
            tab.text = kategori
            tab.isSelected = (kategori == kategoriAktif)
            tab.setOnClickListener {
                kategoriAktif = kategori
                filterMenu()
                for (i in 0 until layoutKategoriTab.childCount) {
                    layoutKategoriTab.getChildAt(i).isSelected = false
                }
                tab.isSelected = true
            }
            layoutKategoriTab.addView(tab)
        }
    }

    private fun setupRvMenu() {
        menuAdapter = MenuTransaksiAdapter(menuTampil) { menu ->
            tambahKePesanan(menu)
        }
        rvMenu.layoutManager = GridLayoutManager(this, 2)
        rvMenu.adapter = menuAdapter
    }

    private fun setupRvPesanan() {
        pesananAdapter = PesananAdapter(
            listPesanan,
            onTambah = { position ->
                val item = listPesanan[position]
                item.jumlah++
                pesananAdapter.notifyItemChanged(position)
                updateTotal()
            },
            onKurang = { position ->
                val item = listPesanan[position]
                if (item.jumlah > 1) {
                    item.jumlah--
                    pesananAdapter.notifyItemChanged(position)
                } else {
                    listPesanan.removeAt(position)
                    pesananAdapter.notifyItemRemoved(position)
                }
                updateTotal()
            }
        )
        rvPesanan.layoutManager = LinearLayoutManager(this)
        rvPesanan.adapter = pesananAdapter
    }

    private fun setupButtons() {

        btnRiwayat.setOnClickListener {
            Toast.makeText(this, "Riwayat Transaksi", Toast.LENGTH_SHORT).show()
        }

        btnBayar.setOnClickListener {
            if (listPesanan.isEmpty()) {
                Toast.makeText(this, "Pesanan masih kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showDialogBayar()
        }

        btnHapusSemua.setOnClickListener {
            if (listPesanan.isEmpty()) return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle("Hapus Semua")
                .setMessage("Yakin ingin menghapus semua pesanan?")
                .setPositiveButton("Ya") { _, _ ->
                    listPesanan.clear()
                    pesananAdapter.notifyDataSetChanged()
                    updateTotal()
                }
                .setNegativeButton("Tidak", null)
                .show()
        }

        btnCetak.setOnClickListener {
            val total = listPesanan.sumOf { it.subtotal }
            cetakStruk(total, listPesanan)
        }
    }

    private fun filterMenu() {
        val etSearch = findViewById<TextInputEditText>(R.id.etSearch)
        val query = etSearch.text.toString().trim().lowercase()

        menuTampil.clear()
        menuTampil.addAll(semuaMenu.filter { menu ->
            val cocokKategori = (kategoriAktif == "Semua" || menu.kategori == kategoriAktif)
            val cocokSearch   = (query.isEmpty() || menu.namaProduk?.lowercase()?.contains(query) == true)
            cocokKategori && cocokSearch
        })
        menuAdapter.notifyDataSetChanged()
    }

    private fun tambahKePesanan(menu: ModelMenu) {
        val existing = listPesanan.indexOfFirst { it.menu.idMenu == menu.idMenu }
        if (existing >= 0) {
            listPesanan[existing].jumlah++
            pesananAdapter.notifyItemChanged(existing)
        } else {
            listPesanan.add(ItemPesanan(menu))
            pesananAdapter.notifyItemInserted(listPesanan.size - 1)
        }
        updateTotal()
        Toast.makeText(this, "${menu.namaProduk} ditambahkan", Toast.LENGTH_SHORT).show()
    }

    private fun ubahJumlah(position: Int, delta: Int) {
        val item = listPesanan[position]
        item.jumlah += delta
        if (item.jumlah <= 0) {
            listPesanan.removeAt(position)
            pesananAdapter.notifyItemRemoved(position)
        } else {
            pesananAdapter.notifyItemChanged(position)
        }
        updateTotal()
    }

    private fun updateTotal() {
        val subtotal = listPesanan.sumOf { it.subtotal }
        val total    = subtotal - diskon

        tvSubtotal.text   = formatRupiah(subtotal)
        tvDiskon.text     = "- ${formatRupiah(diskon)}"
        tvTotal.text      = formatRupiah(total)

        val totalItem = listPesanan.sumOf { it.jumlah }
        tvJumlahItem.text = "$totalItem item"
    }

    private fun showDialogBayar() {
        val total = listPesanan.sumOf { it.subtotal } - diskon
        val view  = layoutInflater.inflate(R.layout.dialog_bayar, null)

        val tvTotalDialog = view.findViewById<TextView>(R.id.tvTotalDialog)
        val etUangBayar   = view.findViewById<TextInputEditText>(R.id.etUangBayar)
        val tvKembalian   = view.findViewById<TextView>(R.id.tvKembalian)

        tvTotalDialog.text = formatRupiah(total)

        etUangBayar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val bayar = s.toString().trim().toLongOrNull() ?: 0L
                val kembalian = bayar - total
                tvKembalian.text = if (kembalian >= 0) formatRupiah(kembalian) else "Kurang ${formatRupiah(-kembalian)}"
                tvKembalian.setTextColor(
                    if (kembalian >= 0)
                        resources.getColor(android.R.color.holo_purple, null)
                    else
                        resources.getColor(android.R.color.holo_red_dark, null)
                )
            }
        })

        val dialog = AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pembayaran")
            .setView(view)
            .setPositiveButton("Bayar", null) // null dulu, override di bawah
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val bayar = etUangBayar.text.toString().trim().toLongOrNull() ?: 0L
                if (bayar < total) {
                    Toast.makeText(this, "Uang bayar kurang!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val catatan = view.findViewById<TextInputEditText>(R.id.etCatatan)
                    .text.toString().trim()
                simpanTransaksi(total, bayar, catatan)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun simpanTransaksi(total: Long, bayar: Long, catatan: String) {
        val transaksiRef = database.getReference("transaksi")
        val penjualanRef = database.getReference("penjualan")
        val id = transaksiRef.push().key ?: ""

        var totalKeuntungan: Long = 0
        for (item in listPesanan) {
            val untungPerItem = item.menu.harga - item.menu.hargaModal
            totalKeuntungan += untungPerItem * item.jumlah
        }

        val timestamp = System.currentTimeMillis()

        val data = mapOf(
            "id" to id,
            "tanggal" to java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(java.util.Date()),
            "timestamp" to timestamp,
            "items" to listPesanan.map { mapOf("idMenu" to it.menu.idMenu, "namaProduk" to it.menu.namaProduk, "jumlah" to it.jumlah, "subtotal" to it.subtotal) },
            "total" to total,
            "bayar" to bayar,
            "kembalian" to (bayar - total),
            "catatan" to catatan
        )

        val dataPenjualan = mapOf(
            "idPenjualan" to id,
            "tanggal" to timestamp,
            "total" to total.toInt(),
            "keuntungan" to totalKeuntungan.toInt()
        )

        // Simpan ke Transaksi dan Penjualan (Laporan)
        transaksiRef.child(id).setValue(data)
        penjualanRef.child(id).setValue(dataPenjualan)

        // Update Stok Otomatis
        for (item in listPesanan) {
            val menuId = item.menu.idMenu
            if (menuId != null) {
                val sisaStok = item.menu.stok - item.jumlah
                menuRef.child(menuId).child("stok").setValue(sisaStok)
            }
        }

        Toast.makeText(this, "Transaksi Berhasil!", Toast.LENGTH_SHORT).show()

            // Tampilkan pilihan cetak setelah sukses simpan
            AlertDialog.Builder(this)
                .setTitle("Transaksi Sukses")
                .setMessage("Apakah ingin mencetak struk?")
                .setPositiveButton("Cetak") { _, _ ->
                    cetakStruk(total, listPesanan)
                    listPesanan.clear()
                    pesananAdapter.notifyDataSetChanged()
                    updateTotal()
                }
                .setNegativeButton("Nanti") { _, _ ->
                    listPesanan.clear()
                    pesananAdapter.notifyDataSetChanged()
                    updateTotal()
                }
                .show()
    }

    private fun formatRupiah(amount: Long): String {
        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return fmt.format(amount).replace(",00", "")
    }

    private fun cetakStruk(totalBayar: Long, listPesanan: List<ItemPesanan>) {
        // 1. Buat konten HTML untuk struk (lebih stabil untuk berbagai jenis printer)
        var htmlContent = "<html><body>" +
                "<h2 style='text-align:center;'>STRUK PENJUALAN</h2>" +
                "<p>Tanggal: ${System.currentTimeMillis()}</p>" +
                "<hr>" +
                "<table style='width:100%'>"

        // Looping item pesanan (menggunakan harga Long yang sudah diperbaiki)
        for (item in listPesanan) {
            htmlContent += "<tr>" +
                    "<td>${item.menu.namaProduk} x${item.jumlah}</td>" +
                    "<td style='text-align:right;'>${item.subtotal}</td>" +
                    "</tr>"
        }

        htmlContent += "</table><hr>" +
                "<h3 style='text-align:right;'>TOTAL: Rp $totalBayar</h3>" +
                "<p style='text-align:center;'>Terima Kasih</p>" +
                "</body></html>"

        // 2. Gunakan WebView untuk proses cetak
        val webView = WebView(this)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Struk_Penjualan")

                printManager.print(
                    "Struk Penjualan",
                    printAdapter,
                    PrintAttributes.Builder().build()
                )
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }

}