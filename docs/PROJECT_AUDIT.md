# PROJECT AUDIT — Smart Market

Tanggal audit: 2026-10-10
Lingkungan: Windows 10 (win32), JDK sistem 25 (Temurin), JDK 21 bundled di `.sdk/jdk/jdk-21.0.12.1+1`, Android SDK 35 + 37, build-tools 34/36, MariaDB 10.4.32 (XAMPP, tidak berjalan), Python 3.13, Docker tidak tersedia.

## 1. Kondisi Awal

Repository `Aplikasi_Coba` adalah **thin-client kiosk** Android Kotlin + Jetpack Compose yang sudah berjalan:

- Application ID / namespace: `com.smartmarket.kiosk`
- Kotlin 2.0.21, AGP 8.7.3, Gradle 8.9 (wrapper), Compose BOM 2024.12.01, Material 3
- `minSdk 24`, `compileSdk 35`, `targetSdk 35`, Java 17 target
- Single-activity Navigation Compose, MVVM sederhana (`ShopViewModel`)
- Tidak ada networking, persistence, atau business logic — semua data mock

## 2. Struktur Source (sebelum perubahan)

```
app/src/main/java/com/smartmarket/kiosk/
├── MainActivity.kt
├── data/
│   ├── model/Models.kt          # Category, CartItem, RecommendedProduct
│   └── mock/MockData.kt         # kategori + 2 item RFID mock (total 35.000)
├── util/FormatUtils.kt          # formatRupiah()
├── ui/
│   ├── components/Common.kt     # Modifier.dashedBorder()
│   ├── navigation/NavGraph.kt   # Routes + NavHost (login/home/cart/checkout)
│   ├── screens/                 # Login, Home, SmartCart, Checkout
│   ├── theme/                   # Color, Type, Theme
│   └── viewmodel/ShopViewModel.kt
```

## 3. Fitur yang Sudah Berjalan

| Fitur | Status | Catatan |
|---|---|---|
| Login (placeholder, "Mulai Belanja") | Berjalan | Tanpa validasi/backend |
| Home (search bar, kategori, rekomendasi) | Berjalan | Data mock |
| Cart (RFID mock, +/- qty, STOP overlay) | Berjalan | Tanpa hapus item |
| Checkout (total + QR placeholder Canvas) | Berjalan | QR non-fungsional |
| Tema Material 3 hijau | Berjalan | |

## 4. Masalah yang Diidentifikasi

1. **JDK sistem adalah 25** — Gradle 8.9 gagal parsing versi Java 25. Harus pakai JDK 21 bundled (`.sdk/jdk/jdk-21.0.12.1+1`).
2. **Tidak ada `INTERNET` permission** di AndroidManifest — wajib untuk Retrofit.
3. **Tidak ada dependency networking/serialization/persistence** (Retrofit, Moshi, DataStore).
4. **Tidak ada autentikasi** — login hanya tombol tanpa kredensial.
5. **Tidak ada katalog produk lengkap** — hanya 1 rekomendasi + 4 kategori.
6. **Tidak ada detail produk, search nyata, filter kategori.**
7. **Cart tidak bisa hapus item** dan tidak ada state kosong yang memadai.
8. **Peta hanya placeholder statis** — tidak ada graph, denah, Dijkstra.
9. **Tidak ada model troli/status troli/navigasi.**
10. **Tidak ada backend.**
11. **Harga memakai `Int`** (aman, tidak floating-point) — akan dipertahankan dan diperluas dengan `BigDecimal`-equivalent (tetap integer rupiah) untuk konsistensi.
12. **Tidak ada test.**

## 5. Keputusan Arsitektur

- Pertahankan: Kotlin, Compose, Material 3, Navigation Compose, MVVM, tema hijau, package `com.smartmarket.kiosk`.
- Tambahkan: Retrofit + OkHttp, Moshi (JSON), DataStore Preferences (sesi), Coroutines (sudah ada), unit test (JUnit + coroutines-test).
- Repositori dibuat sebagai **interface + demo implementation** agar dapat diganti remote tanpa mengubah UI (sesuai §6C master prompt).
- Uang tetap **integer rupiah** (`Int`) — bebas floating-point error (sesuai §6D).
- Backend: **FastAPI + SQLAlchemy 2.0**, target MySQL (MariaDB 10.4 kompatibel), **fallback SQLite untuk test** karena server MariaDB tidak berjalan di lingkungan (di dokumentasi sebagai batasan, bukan penggantian diam-diam).

## 6. Rencana Perubahan

1. Fondasi: dependency + manifest INTERNET + baseline build hijau.
2. Graph & Dijkstra + denah demo (model data, bukan koordinat hardcoded di UI).
3. Katalog produk + mapping produk→lokasi + repository abstraksi.
4. UI lengkap: splash, auth (login/register), home, katalog, detail, cart, peta+navigasi, status troli, profil.
5. Backend FastAPI + schema + seed + endpoint + test.
6. Integrasi Retrofit (login/katalog) dengan fallback demo.
7. Abstraksi `TrolleyNavigationRepository` + demo impl + adapter placeholder.
8. Finalisasi: build, test, dokumentasi lengkap.

## 7. Dokumen Referensi

- `Development_of_Smart_Trolley_System_Based_on_Andro (1).pdf` (Procedia CS 157/2019, Gunawan et al.) — arsitektur troli diferensial, IOIO, Navisens (IPS inersia), Firebase binding, NaviServer UDP. **Tidak menyebut** ESP32-S3, UWB, maupun Dijkstra; dokumen ini adalah konteks historis troli otomatis, bukan kontrak firmware proyek ini.
- Dokumen `ESP32 UWB Positioning` dan `Smart Trolley Autonomous Navigation System` yang disebut master prompt **tidak tersedia** di repository. Tidak ada klaim format telemetri/firmware yang dibuat; integrasi hardware diisolasi di balik interface dan ditandai "belum terverifikasi".

## 8. Status Ketersediaan Lingkungan

| Komponen | Status |
|---|---|
| JDK 21 (bundled) | ✅ tersedia |
| Android SDK 35/37 | ✅ tersedia |
| Gradle wrapper 8.9 | ✅ tersedia |
| Python 3.13 + pip | ✅ tersedia |
| MariaDB 10.4 (XAMPP) | ⚠️ terinstall, server tidak berjalan |
| Docker | ❌ tidak tersedia |
| Emulator/perangkat | ⚠️ tidak dikonfirmasi (build APK memungkinkan) |
