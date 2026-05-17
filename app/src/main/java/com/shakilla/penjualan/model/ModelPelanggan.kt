package com.shakilla.penjualan.model

import java.io.Serializable

data class ModelPelanggan(
    val idPelanggan: String? = null,
    val namaPelanggan: String? = null,
    val preferensi: String? = null,
    val keluhan: String? = null,
    val feedback: String? = null,
    val tanggalCatatan: Long? = 0
) : Serializable
