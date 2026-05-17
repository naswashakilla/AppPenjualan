package com.shakilla.penjualan.model

import java.io.Serializable

data class ModelServis(
    val idServis: String? = null,
    val namaPelanggan: String? = null,
    val namaBarang: String? = null,
    val deskripsiKerusakan: String? = null,
    val biaya: Long = 0,
    val status: String? = "Proses", // Proses, Selesai, Diambil
    val tanggalMasuk: Long? = 0
) : Serializable
