package com.shakilla.penjualan.model

data class ModelMenu(
    val idMenu: String? = null,
    val namaProduk: String? = null,
    val harga: String? = null,
    val stok: String? = null,
    val cabang: String? = null,
    val kategori: String? = null,
    val status: String? = null,
    val urlFoto: String? = null
) {
    // Konstruktor kosong untuk Firebase
    constructor() : this("", "", "", "", "", "", "")
}