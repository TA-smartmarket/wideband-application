# Smart Market — Android (Kotlin + XML)

Aplikasi Android troli belanja pintar. **Tanpa login/registrasi.** Dibuka pada
layar idle/welcome; tombol **Mulai Belanja** memulai sesi belanja lokal.

## Alur

1. **Idle** — "Selamat Datang di Smart Market", indikator koneksi UWB, tombol
   Mulai Belanja.
2. **Beranda** — pencarian, kategori, promo, rekomendasi, status troli, akses
   peta & keranjang.
3. **Katalog** — pencarian + filter kategori (data demo lokal).
4. **Detail produk** — deskripsi, harga, lokasi rak, cari lokasi di peta
   (tanpa tombol tambah/jumlah manual).
5. **Keranjang** — isi berasal dari konfirmasi sistem RFID/backend; subtotal &
   total; navigasi ke rak. Tidak ada kontrol kuantitas manual.
6. **Peta & navigasi** — denah demo, pilih tujuan, rute Dijkstra, marker troli
   (simulasi), posisi UWB nyata bila server tersedia. Status positioning (UWB)
   dan status navigasi (troli) ditampilkan terpisah.
7. **Akhiri Belanja** — kembali ke idle; keranjang dipertahankan (perilaku lama).

## Teknologi

- Kotlin 2.0, Android Views + **layout XML** (bisa diedit lewat Layout Editor),
  View Binding.
- Material Components, tanpa Jetpack Compose.
- Uang integer Rupiah (`Int`).
- Dijkstra murni Kotlin (unit-tested).
- OkHttp untuk polling server UWB.

## Struktur sumber

```
app/src/main/java/com/smartmarket/kiosk/
├── MainActivity.kt            # single-activity host + bottom nav + sesi
├── ShopNavigator.kt           # kontrak navigasi antar-layar
├── SmartMarketApplication.kt  # pemilik AppContainer
├── core/di/AppContainer.kt    # manual DI: catalog, troli, UWB, keranjang
├── data/
│   ├── model/                 # Product, Category, CartItem, Graph, Node, Edge…
│   ├── local/                 # DemoCatalog (18 produk), DemoMap (denah)
│   ├── algorithm/Dijkstra.kt  # shortest path (murni Kotlin)
│   ├── repository/            # CatalogRepository + demo impl; CartRepository (seam RFID)
│   └── uwb/                   # UwbPositioningRepository (GET /api/v1/state)
├── integration/               # TrolleyNavigationRepository + simulator
├── ui/                        # Fragment + Adapter + MapView (custom Canvas)
└── util/FormatUtils.kt        # formatRupiah
app/src/main/res/layout/       # activity_main + fragment_* + item_* (XML)
```

## Integrasi UWB (positioning)

- Server Flask `wideband-positioning` (`GET /api/v1/state`) dipolling tiap 1 detik.
- Tag troli dapat dikonfigurasi lewat Gradle (`UWB_TAG_ID`, default `tag-1`).
- Alamat server dapat dikonfigurasi lewat `UWB_BASE_URL`; default
  `http://10.0.2.2:8080/` (localhost host dari **emulator**). Untuk HP fisik di
  LAN, build dengan:
  `./gradlew.bat assembleDebug -PUWB_BASE_URL=http://<ip-lan>:8080/`.
- Posisi UWB ditampilkan hanya saat tag online dan belum usang; tanpa server
  tampil "Tidak terhubung" (tidak ada koordinat palsu). Posisi troli berasal dari
  tag UWB, **bukan** GPS HP.
- Data UWB vs demo dibedakan dengan label eksplisit.
- Dimensi ruangan memakai `room.width` × `room.depth` dari kontrak `GET /api/v1/state`
  (bukan `room.height`, yang merupakan tinggi plafon/Z).

## Integrasi navigasi (troli)

- Navigasi fisik **tidak** terhubung; `TrolleyNavigationRepository` adalah seam
  tunggal. `DemoTrolleyNavigationRepository` (simulasi, label "Simulasi") memajukan
  marker; `HardwareTrolleyNavigationRepository` adalah placeholder yang **tidak**
  mengirim perintah motor sampai kontrak firmware diverifikasi.
- Status positioning (UWB) dan status navigasi (troli) dirender terpisah di layar
  Peta; `POSITION_LOST` dipetakan dari telemetri navigasi ("kehilangan posisi —
  berhenti aman").

## Keranjang (RFID)

- `CartRepository` adalah seam untuk sumber isi keranjang yang sah (RFID/backend).
- Saat ini terpasang `PendingCartRepository` (sumber kosong); layar Keranjang
  menampilkan "Menunggu koneksi RFID" tanpa transaksi palsu. Kontrol kuantitas
  manual (`+`/`−`, hapus, tambah) telah dihapus dari detail produk dan keranjang.

## Build & test

```bash
# JDK 21 bundled (JDK sistem 25 tidak didukung Gradle 8.9)
export JAVA_HOME="C:/SMART TROLLEY/Aplikasi_Coba/.sdk/jdk/jdk-21.0.12.1+1"
export PATH="$JAVA_HOME/bin:$PATH"

./gradlew.bat assembleDebug          # APK debug
./gradlew.bat testDebugUnitTest      # unit test Dijkstra (10 kasus)
```

Jalankan aplikasi → buka lewat Android Studio → Run pada emulator/perangkat:
layar pertama adalah idle/welcome; tekan **Mulai Belanja**.