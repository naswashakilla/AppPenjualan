# Saji.id - Smart POS System 🚀

**Saji.id** adalah aplikasi Kasir (Point of Sale) berbasis Android yang dirancang untuk membantu pengelolaan bisnis UMKM, Cafe, dan Restoran secara modern, efisien, dan terintegrasi dengan teknologi Cloud.

## ✨ Fitur Utama
- **Dashboard Modern:** Tampilan Dashboard profesional dengan estimasi pendapatan real-time (Support Portrait & Landscape).
- **Manajemen Inventaris:** Kelola menu, kategori produk, dan stok barang dengan mudah.
- **Transaksi Cepat:** Proses penjualan yang intuitif dengan fitur diskon dan manajemen pesanan.
- **Multi-Cabang:** Mendukung pengelolaan data untuk banyak cabang toko.
- **Cetak Struk Bluetooth:** Integrasi langsung dengan printer termal Bluetooth untuk mencetak nota belanja secara instan.
- **Manajemen Pegawai:** Pendataan kasir/pegawai lengkap dengan foto profil.
- **Sinkronisasi Cloud:** Menggunakan Firebase Realtime Database untuk data yang selalu sinkron dan Firebase Storage untuk penyimpanan foto produk.
- **Mode Gelap/Terang:** Dukungan tema Day/Night yang menyesuaikan dengan kenyamanan mata pengguna.

## 📱 Screenshots

<p align="center">
  <img src="app/src/main/res/drawable/halaman.jpeg" width="200" title="Dashboard">
  <img src="app/src/main/res/drawable/transaksi.jpeg" width="200" title="Transaksi">
  <img src="app/src/main/res/drawable/catatan.jpeg" width="200" title="Catatan">
  <img src="app/src/main/res/drawable/tambahcatatan.jpeg" width="200" title="Tambah Catatan">
  <img src="app/src/main/res/drawable/laporan.jpeg" width="200" title="Laporan">
  <img src="app/src/main/res/drawable/login.png" width="200" title="Login">
  <img src="app/src/main/res/drawable/kategori.jpeg" width="200" title="Data Kategori">
  <img src="app/src/main/res/drawable/menu.jpeg" width="200" title="Daftar Menu">
  <img src="app/src/main/res/drawable/tambahmenu.jpeg" width="200" title="Manajemen Menu">
  <img src="app/src/main/res/drawable/pegawai.jpeg" width="200" title="Data Pegawai">
  <img src="app/src/main/res/drawable/tambahpegawai.jpeg" width="200" title="Manajemen Pegawai">
  <img src="app/src/main/res/drawable/layanan.jpeg" width="200" title="Layanan">
  <img src="app/src/main/res/drawable/servis.jpeg" width="200" title="Servis">
  <img src="app/src/main/res/drawable/tambahservis.jpeg" width="200" title="Tambah Servis">
  <img src="app/src/main/res/drawable/promo.jpeg" width="200" title="Promo">
  <img src="app/src/main/res/drawable/ulasan.jpeg" width="200" title="Rating dan Ulasan">
  <img src="app/src/main/res/drawable/cabang.jpeg" width="200" title="Cabang">
  <img src="app/src/main/res/drawable/prin.jpeg" width="200" title="Printer">
</p>

## 🛠️ Teknologi yang Digunakan
- **Bahasa:** Kotlin
- **UI Framework:** Material Components (Material 3)
- **Database:** Firebase Realtime Database
- **Storage:** Firebase Storage
- **Authentication:** Firebase Auth
- **Library Pihak Ketiga:** 
    - [Glide](https://github.com/bumptech/glide) untuk pemuatan gambar.
    - WebView untuk rendering nota HTML.

## 🚀 Cara Instalasi
1. Clone repository ini.
2. Hubungkan project dengan Firebase Console Anda.
3. Unduh file `google-services.json` dan letakkan di folder `app/`.
4. Aktifkan Firebase Auth, Realtime Database, dan Storage.
5. Build dan jalankan aplikasi di Android Studio.