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

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.OutputStream
import java.util.UUID

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.Manifest
import android.os.Build

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
    private val listNamaKasir = mutableListOf<String>()
    private var kategoriAktif = "Semua"
    private var diskon: Long  = 0L
    
    // Data untuk kebutuhan cetak (Snapshot)
    private var totalTerakhir: Long = 0
    private var bayarTerakhir: Long = 0
    private var kembaliTerakhir: Long = 0
    private var subtotalTerakhir: Long = 0
    private var diskonTerakhir: Long = 0
    private var namaKasirAktif = "-"
    private var listPesananCetak = mutableListOf<ItemPesanan>()

    private val database = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val menuRef = database.getReference("menu")
    private val pegawaiRef = database.getReference("pegawai")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaksi)

        initViews()
        setupRvMenu()
        setupRvPesanan()
        setupButtons()
        loadMenuDariFirebase()
        loadPegawaiDariFirebase()
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

        val etSearch = findViewById<TextInputEditText>(R.id.etSearch)
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
                    if (menu != null && menu.status == "1") semuaMenu.add(menu)
                }
                pbLoading.visibility = View.GONE
                kategoriList.clear()
                kategoriList.add("Semua")
                kategoriList.addAll(semuaMenu.mapNotNull { it.kategori }.filter { it.isNotBlank() }.distinct())
                menuTampil.clear()
                menuTampil.addAll(semuaMenu)
                menuAdapter.notifyDataSetChanged()
                setupKategoriTab()
            }
            override fun onCancelled(error: DatabaseError) {
                pbLoading.visibility = View.GONE
            }
        })
    }

    private fun loadPegawaiDariFirebase() {
        pegawaiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listNamaKasir.clear()
                listNamaKasir.add("- Pilih Kasir -")
                for (item in snapshot.children) {
                    val nama = item.child("nama").getValue(String::class.java)
                    if (nama != null) listNamaKasir.add(nama)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupKategoriTab() {
        layoutKategoriTab.removeAllViews()
        kategoriList.forEach { kategori ->
            val tab = LayoutInflater.from(this).inflate(R.layout.item_kategori_tab, layoutKategoriTab, false) as TextView
            tab.text = kategori
            tab.isSelected = (kategori == kategoriAktif)
            tab.setOnClickListener {
                kategoriAktif = kategori
                filterMenu()
                for (i in 0 until layoutKategoriTab.childCount) layoutKategoriTab.getChildAt(i).isSelected = false
                tab.isSelected = true
            }
            layoutKategoriTab.addView(tab)
        }
    }

    private fun setupRvMenu() {
        menuAdapter = MenuTransaksiAdapter(menuTampil) { tambahKePesanan(it) }
        rvMenu.layoutManager = GridLayoutManager(this, 2)
        rvMenu.adapter = menuAdapter
    }

    private fun setupRvPesanan() {
        pesananAdapter = PesananAdapter(listPesanan, 
            onTambah = { pos -> listPesanan[pos].jumlah++; pesananAdapter.notifyItemChanged(pos); updateTotal() },
            onKurang = { pos -> 
                if (listPesanan[pos].jumlah > 1) { listPesanan[pos].jumlah--; pesananAdapter.notifyItemChanged(pos) }
                else { listPesanan.removeAt(pos); pesananAdapter.notifyItemRemoved(pos) }
                updateTotal()
            }
        )
        rvPesanan.layoutManager = LinearLayoutManager(this)
        rvPesanan.adapter = pesananAdapter
    }

    private fun setupButtons() {
        btnRiwayat.setOnClickListener { Toast.makeText(this, "Riwayat Transaksi", Toast.LENGTH_SHORT).show() }
        btnBayar.setOnClickListener { if (listPesanan.isEmpty()) Toast.makeText(this, "Pesanan kosong!", Toast.LENGTH_SHORT).show() else showDialogBayar() }
        btnHapusSemua.setOnClickListener {
            if (listPesanan.isEmpty()) return@setOnClickListener
            AlertDialog.Builder(this).setTitle("Hapus").setMessage("Hapus semua?").setPositiveButton("Ya") { _, _ ->
                listPesanan.clear(); pesananAdapter.notifyDataSetChanged(); updateTotal()
            }.setNegativeButton("Tidak", null).show()
        }
        btnCetak.setOnClickListener { 
            if (listPesananCetak.isEmpty()) {
                Toast.makeText(this, "Belum ada transaksi untuk dicetak!", Toast.LENGTH_SHORT).show()
            } else {
                pilihMetodeCetak()
            }
        }
    }

    private fun filterMenu() {
        val query = findViewById<TextInputEditText>(R.id.etSearch).text.toString().trim().lowercase()
        menuTampil.clear()
        menuTampil.addAll(semuaMenu.filter { (kategoriAktif == "Semua" || it.kategori == kategoriAktif) && (query.isEmpty() || it.namaProduk?.lowercase()?.contains(query) == true) })
        menuAdapter.notifyDataSetChanged()
    }

    private fun tambahKePesanan(menu: ModelMenu) {
        val existing = listPesanan.indexOfFirst { it.menu.idMenu == menu.idMenu }
        if (existing >= 0) { listPesanan[existing].jumlah++; pesananAdapter.notifyItemChanged(existing) }
        else { listPesanan.add(ItemPesanan(menu)); pesananAdapter.notifyItemInserted(listPesanan.size - 1) }
        updateTotal()
    }

    private fun updateTotal() {
        val subtotal = listPesanan.sumOf { it.subtotal }
        val total = subtotal - diskon
        tvSubtotal.text = formatRupiah(subtotal)
        tvDiskon.text = "- ${formatRupiah(diskon)}"
        tvTotal.text = formatRupiah(total)
        tvJumlahItem.text = "${listPesanan.sumOf { it.jumlah }} item"
    }

    private fun formatRupiah(amount: Long): String {
        return NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(amount).replace(",00", "")
    }

    private fun showDialogBayar() {
        val total = listPesanan.sumOf { it.subtotal } - diskon
        val view = layoutInflater.inflate(R.layout.dialog_bayar, null)
        val etUangBayar = view.findViewById<TextInputEditText>(R.id.etUangBayar)
        val tvKembalian = view.findViewById<TextView>(R.id.tvKembalian)
        val spinnerKasir = view.findViewById<Spinner>(R.id.spinnerKasir)
        view.findViewById<TextView>(R.id.tvTotalDialog).text = formatRupiah(total)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listNamaKasir)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKasir.adapter = spinnerAdapter

        etUangBayar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val bayar = s.toString().toLongOrNull() ?: 0L
                val kembalian = bayar - total
                tvKembalian.text = if (kembalian >= 0) formatRupiah(kembalian) else "Kurang"
            }
        })

        val dialog = AlertDialog.Builder(this).setTitle("Bayar").setView(view).setPositiveButton("Bayar", null).setNegativeButton("Batal", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val bayar = etUangBayar.text.toString().toLongOrNull() ?: 0L
                val kasir = spinnerKasir.selectedItem.toString()
                if (bayar < total) Toast.makeText(this, "Uang kurang!", Toast.LENGTH_SHORT).show()
                else if (kasir == "- Pilih Kasir -") Toast.makeText(this, "Pilih kasir!", Toast.LENGTH_SHORT).show()
                else {
                    namaKasirAktif = kasir
                    simpanTransaksi(total, bayar, view.findViewById<TextInputEditText>(R.id.etCatatan).text.toString(), kasir)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun simpanTransaksi(total: Long, bayar: Long, catatan: String, kasir: String) {
        val id = database.getReference("transaksi").push().key ?: ""
        val kembalian = bayar - total
        
        // Simpan Snapshot Data untuk Kebutuhan Cetak
        subtotalTerakhir = listPesanan.sumOf { it.subtotal }
        diskonTerakhir = diskon
        totalTerakhir = total
        bayarTerakhir = bayar
        kembaliTerakhir = kembalian
        listPesananCetak.clear()
        listPesananCetak.addAll(listPesanan)

        val data = mapOf(
            "id" to id, 
            "tanggal" to java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(java.util.Date()), 
            "total" to total, 
            "bayar" to bayar, 
            "kembalian" to kembalian,
            "kasir" to kasir, 
            "items" to listPesanan.map { mapOf("namaProduk" to it.menu.namaProduk, "jumlah" to it.jumlah, "subtotal" to it.subtotal) }
        )
        
        database.getReference("transaksi").child(id).setValue(data).addOnSuccessListener {
            Toast.makeText(this, "Transaksi Berhasil!", Toast.LENGTH_SHORT).show()
            pilihMetodeCetak()
            listPesanan.clear(); pesananAdapter.notifyDataSetChanged(); updateTotal()
        }
    }

    private fun pilihMetodeCetak() {
        val options = arrayOf("Cetak Langsung Bluetooth", "Cetak via Sistem Android")
        AlertDialog.Builder(this).setTitle("Pilih Metode Cetak")
            .setItems(options) { _, which ->
                if (which == 0) temukanPrinterBluetooth() else cetakStrukSistem()
            }.show()
    }

    private fun temukanPrinterBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 101)
                return
            }
        }

        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Aktifkan Bluetooth Anda!", Toast.LENGTH_SHORT).show()
            return
        }

        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "Pasangkan (pair) printer Bluetooth di pengaturan HP dulu!", Toast.LENGTH_LONG).show()
            return
        }

        val deviceNames = pairedDevices.map { it.name ?: "Unknown Device" }.toTypedArray()
        val devices = pairedDevices.toList()

        AlertDialog.Builder(this).setTitle("Pilih Printer")
            .setItems(deviceNames) { _, i -> cetakBluetoothLangsung(devices[i]) }.show()
    }

    private fun cetakBluetoothLangsung(device: BluetoothDevice) {
        Thread {
            var socket: BluetoothSocket? = null
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"))
                    socket.connect()
                    val out = socket.outputStream
                    
                    val boldOn = byteArrayOf(0x1B, 0x45, 0x01)
                    val boldOff = byteArrayOf(0x1B, 0x45, 0x00)
                    val center = byteArrayOf(0x1B, 0x61, 0x01)
                    val left = byteArrayOf(0x1B, 0x61, 0x00)

                    // Header
                    out.write(center); out.write(boldOn)
                    out.write("SAJI.ID\n".toByteArray())
                    out.write(boldOff)
                    out.write("Solo, Jawa Tengah\n".toByteArray())
                    out.write("--------------------------------\n".toByteArray())
                    
                    // Info Transaksi
                    out.write(left)
                    out.write("Tgl  : ${java.text.SimpleDateFormat("dd/MM/yy HH:mm").format(java.util.Date())}\n".toByteArray())
                    out.write("Kasir: $namaKasirAktif\n".toByteArray())
                    out.write("--------------------------------\n".toByteArray())

                    // Items
                    for (item in listPesananCetak) {
                        out.write("${item.menu.namaProduk}\n".toByteArray())
                        val lineItem = "  ${item.jumlah} x ${item.menu.harga}"
                        val lineTotal = item.subtotal.toString()
                        val spaceCount = 32 - lineItem.length - lineTotal.length
                        out.write((lineItem + " ".repeat(if (spaceCount > 0) spaceCount else 1) + lineTotal + "\n").toByteArray())
                    }

                    out.write("--------------------------------\n".toByteArray())
                    
                    // Rincian Pembayaran
                    val labelSubtotal = "Subtotal:"
                    val valSubtotal = subtotalTerakhir.toString()
                    val s1 = 32 - labelSubtotal.length - valSubtotal.length
                    out.write((labelSubtotal + " ".repeat(if (s1 > 0) s1 else 1) + valSubtotal + "\n").toByteArray())

                    if (diskonTerakhir > 0) {
                        val labelDiskon = "Diskon:"
                        val valDiskon = "-$diskonTerakhir"
                        val s2 = 32 - labelDiskon.length - valDiskon.length
                        out.write((labelDiskon + " ".repeat(if (s2 > 0) s2 else 1) + valDiskon + "\n").toByteArray())
                    }

                    out.write(boldOn)
                    val labelTotal = "TOTAL:"
                    val valTotal = formatRupiah(totalTerakhir)
                    val s3 = 32 - labelTotal.length - valTotal.length
                    out.write((labelTotal + " ".repeat(if (s3 > 0) s3 else 1) + valTotal + "\n").toByteArray())
                    out.write(boldOff)

                    out.write("--------------------------------\n".toByteArray())
                    
                    val labelBayar = "Tunai:"
                    val valBayar = bayarTerakhir.toString()
                    val s4 = 32 - labelBayar.length - valBayar.length
                    out.write((labelBayar + " ".repeat(if (s4 > 0) s4 else 1) + valBayar + "\n").toByteArray())

                    val labelKembali = "Kembali:"
                    val valKembali = kembaliTerakhir.toString()
                    val s5 = 32 - labelKembali.length - valKembali.length
                    out.write((labelKembali + " ".repeat(if (s5 > 0) s5 else 1) + valKembali + "\n").toByteArray())

                    out.write(center)
                    out.write("\n  *** TERIMA KASIH ***\n\n\n\n\n".toByteArray())
                    
                    out.flush()
                    runOnUiThread { Toast.makeText(this, "Mencetak...", Toast.LENGTH_SHORT).show() }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_LONG).show() }
            } finally {
                socket?.close()
            }
        }.start()
    }

    private fun cetakStrukSistem() {
        val webView = WebView(this)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(v: WebView, u: String) {
                val pm = getSystemService(Context.PRINT_SERVICE) as PrintManager
                pm.print("Nota", webView.createPrintDocumentAdapter("Nota"), PrintAttributes.Builder().build())
            }
        }
        val html = "<html><body><center><h1>SAJI.ID</h1><p>$namaKasirAktif</p><hr><p>TOTAL: ${formatRupiah(totalTerakhir)}</p></center></body></html>"
        webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
    }
}
