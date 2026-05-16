package com.shakilla.penjualan

data class ModelTransaksi(
    val id: String? = null,
    val tanggal: String? = null,
    val total: Long = 0,
    val bayar: Long = 0,
    val kembalian: Long = 0,
    val catatan: String? = null,
    val items: List<Map<String, Any>>? = null
)