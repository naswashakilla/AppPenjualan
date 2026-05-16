package com.shakilla.penjualan.model

data class ModelMenu(
    val idMenu: String = "",
    val namaProduk: String = "",
    val harga: Long = 0,
    val stok: Int = 0,
    val cabang: String = "",
    val kategori: String = "",
    val status: String = "",
    val urlFoto: String = ""
) {
    // Firebase membutuhkan konstruktor tanpa argumen.
    // Dengan memberikan nilai default di atas, Kotlin otomatis menyediakannya.
}