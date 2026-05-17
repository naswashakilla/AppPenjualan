package com.shakilla.penjualan.model

import java.io.Serializable

data class ModelRating(
    val idRating: String? = null,
    val namaPelanggan: String? = null,
    val rating: Float = 0f,
    val ulasan: String? = null,
    val tanggal: Long? = 0
) : Serializable
