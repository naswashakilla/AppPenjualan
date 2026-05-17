package com.shakilla.penjualan.model

data class ModelPenjualan(
    var idPenjualan: String? = null,
    var tanggal: Long? = 0,
    var total: Long? = 0,
    var keuntungan: Long? = 0
)