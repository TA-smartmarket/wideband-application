package com.smartmarket.kiosk.data.uwb

import java.util.Locale

/** Human-readable labels for UWB connection/position quality. */
fun UwbState.statusLabel(): String = when {
    !connected -> "Tidak terhubung ke server UWB"
    !tagOnline -> "Tag UWB offline"
    stale -> "Posisi usang (tidak real-time)"
    ambiguous -> "Posisi ambigu (perlu anchor tambahan)"
    x != null && y != null -> "Posisi: (${fmt(x)} m, ${fmt(y)} m)"
    else -> "Posisi belum tersedia"
}

fun UwbState.qualityLabel(): String = when {
    !connected || !tagOnline -> ""
    confidence != null -> {
        val pct = (confidence * 100).toInt().coerceIn(0, 100)
        "Confidence ${pct}% · ${if (ambiguous) "ambigu" else "valid"}"
    }
    else -> ""
}

fun UwbState.sourceLabel(): String = when (source) {
    UwbSource.REAL -> "UWB (nyata)"
    UwbSource.NONE -> "Tidak ada data UWB"
}

fun UwbState.staleLabel(): String = if (stale) " · Data usang" else ""

private fun fmt(v: Float): String = String.format(Locale.US, "%.2f", v)