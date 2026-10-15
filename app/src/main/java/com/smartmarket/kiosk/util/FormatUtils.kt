package com.smartmarket.kiosk.util

import java.text.NumberFormat
import java.util.Locale

private val rupiahFormatter = NumberFormat.getIntegerInstance(Locale("id", "ID"))

/** Formats an integer amount into Indonesian Rupiah, e.g. 35000 -> "Rp 35.000". */
fun formatRupiah(amount: Int): String = "Rp ${rupiahFormatter.format(amount)}"
