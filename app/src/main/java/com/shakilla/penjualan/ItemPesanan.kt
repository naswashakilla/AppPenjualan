package com.shakilla.penjualan

import com.shakilla.penjualan.model.ModelMenu

data class ItemPesanan(
    val menu: ModelMenu,
    var jumlah: Int = 1
) {
    val subtotal: Long get() = menu.harga * jumlah
}