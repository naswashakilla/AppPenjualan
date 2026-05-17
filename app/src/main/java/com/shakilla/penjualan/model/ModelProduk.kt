package com.shakilla.penjualan.model

data class ModelProduk(
    var idProduk: String? = null,
    var namaProduk: String? = null,
    var stokProduk: Int? = 0,
    var hargaModal: Int? = 0,
    var hargaJual: Int? = 0
)