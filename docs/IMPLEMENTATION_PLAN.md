# IMPLEMENTATION PLAN — Smart Market

Rencana implementasi dari kondisi audit ke target akhir. Fase mengikuti master prompt.

## Fase 1 — Audit ✅
- Membaca source code, dua dokumen (hanya PDF historis tersedia), konfigurasi Gradle.
- `docs/PROJECT_AUDIT.md` ditulis.
- Baseline build diverifikasi dengan JDK 21 bundled.

## Fase 2 — Fondasi Android
- Tambah dependency: Retrofit, OkHttp, Moshi, DataStore Preferences, JUnit + coroutines-test.
- Tambah `INTERNET` permission di manifest.
- Struktur package: `core`, `data` (model/local/remote/repository), `domain`, `ui`, `integration` (interface troli).
- DI manual via `AppContainer` (Application-scoped) — tanpa framework DI, minim risiko build.

## Fase 3 — Graph & Dijkstra
- `data/model/Graph.kt`: `Node`, `Edge`, `Shelf`, `Destination`, `MapGraph`, `Route`.
- `data/algorithm/Dijkstra.kt`: pure-Kotlin, `PathResult` (Success/Unreachable/InvalidNode/EmptyGraph).
- `data/local/DemoMap.kt`: denah demo (rak, lorong, node, edge berbobot, tujuan, titik awal).
- Unit test `DijkstraTest`.

## Fase 4 — Fitur Belanja
- `data/model/Models.kt`: `Product`, `ProductLocation` (mapping produk→rak→tujuan→node).
- `data/local/DemoCatalog.kt`: katalog demo + mapping lokasi.
- Repository interface + demo impl + remote impl (Retrofit).
- ViewModel: `AuthViewModel`, `CatalogViewModel`, `CartViewModel`, `MapViewModel`, `TrolleyViewModel`.
- UI: Splash, Login, Register, Home, Catalog, ProductDetail, Cart, Map/Navigation, TrolleyStatus, Profile, Checkout.

## Fase 5 — Backend
- FastAPI + SQLAlchemy 2.0; target MySQL (MariaDB), fallback SQLite untuk test.
- Auth (register/login/me/logout, pbkdf2 hashing, JWT HS256), Product, Order, Trolley.
- Schema/migrasi + seed + `.env.example` + pytest.

## Fase 6 — Integrasi
- Retrofit client + DTO + remote repository; login & katalog terhubung; fallback demo eksplisit saat backend tidak tersedia.

## Fase 7 — Troli
- `integration/TrolleyNavigationRepository` (interface) + `DemoTrolleyNavigationRepository`.
- Adapter hardware hanya placeholder berbasis kontrak terdokumentasi (belum diverifikasi → tidak mengirim perintah motor).

## Fase 8 — Finalisasi
- `assembleDebug` + `testDebugUnitTest` + `pytest`.
- Dokumentasi lengkap + README + laporan akhir.

## Keputusan kunci
| Area | Keputusan | Alasan |
|---|---|---|
| Uang | `Int` rupiah | Bebas floating-point error (§6D) |
| Serialisasi | Moshi (reflection) | Tanpa compiler plugin, build stabil |
| DI | Manual `AppContainer` | Tanpa Hilt/ksp, minim risiko |
| Sesi | DataStore Preferences | Standar; token bukan password; produksi → EncryptedSharedPreferences |
| Backend DB | MySQL target, SQLite test fallback | MariaDB server tidak berjalan di lingkungan; didokumentasikan |
