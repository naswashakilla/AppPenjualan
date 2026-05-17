package com.shakilla.penjualan.model

data class ModelMenu(
    val idMenu: String? = null,
    val namaProduk: String? = null,
    val harga: Long = 0,
    val hargaModal: Long = 0,
    val stok: Int = 0,
    val kategori: String? = null,
    val status: String? = "1",
    val urlFoto: String? = null,
    val listCabang: List<String>? = null
) : java.io.Serializable