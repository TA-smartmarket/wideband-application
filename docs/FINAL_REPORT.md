# Laporan Akhir — Smart Market

Tanggal: 2026-10-11

## Ringkasan

Aplikasi kiosk Android thin-client diubah menjadi aplikasi **Smart Market**
lengkap: autentikasi, katalog/keranjang, denah + navigasi Dijkstra, simulasi
status troli, dan backend FastAPI + MySQL (fallback SQLite untuk test).

## Hasil verifikasi (benar-benar dijalankan)

| Item | Perintah | Hasil |
|---|---|---|
| Build Android | `gradlew.bat assembleDebug` (JDK 21) | ✅ BUILD SUCCESSFUL |
| Unit test Dijkstra | `gradlew.bat testDebugUnitTest` | ✅ 10/10 pass, 0 failure |
| Backend test | `pytest -q` | ✅ 26 passed |
| Smoke backend | `uvicorn app.main:app` (SQLite) + `GET /health`, `GET /api/v1/products` | ✅ `{"status":"ok"}`, 18 produk |
| Smoke backend (MySQL default) | `uvicorn` tanpa `DATABASE_URL` override | ⚠️ gagal startup — MariaDB tidak berjalan (diharapkan, terdokumentasi) |

## Yang selesai

### Android
- Fondasi: Retrofit + OkHttp + Moshi (reflection) + DataStore + coroutines-test;
  `INTERNET` permission; `BuildConfig.API_BASE_URL`.
- Data: model produk/kategori/keranjang; model graph (`Node`, `Edge`, `Shelf`,
  `Destination`, `MapGraph`, `Route`); Dijkstra murni Kotlin dengan
  `PathResult` sealed; `DemoMap` (grid 3×3, 7 tujuan, 6 rak); `DemoCatalog`
  (18 produk + mapping produk→lokasi).
- Repository: `AuthRepository`, `CatalogRepository`, `OrderRepository` +
  implementasi remote (backend) dan demo (fallback berlabel).
- DI: `AppContainer` + `SmartMarketApplication` + `CompositionLocal`.
- Session: `SessionManager` (DataStore; token + profil, tanpa password).
- ViewModel: `AuthViewModel`, `CatalogViewModel`, `CartViewModel`,
  `MapViewModel`, `CheckoutViewModel`.
- UI: Splash, Auth (login/register), Home, Catalog, ProductDetail, Cart,
  Checkout, Map (Canvas + legenda + kontrol navigasi), TrolleyStatus, Profile.
- Navigasi: `NavGraph` + route + `ViewModelFactories`.
- Troli: `TrolleyNavigationRepository` + `DemoTrolleyNavigationRepository` +
  `HardwareTrolleyNavigationRepository` (placeholder terdokumentasi).

### Backend (`backend/`)
- FastAPI + SQLAlchemy 2.0 + pydantic v2; MySQL-first, SQLite fallback test.
- Security: pbkdf2_hmac (stdlib, 200k iter, salt acak) + JWT HS256 (PyJWT).
- Router: `auth` (register/login/me/logout), `products`, `orders`,
  `trolleys`; seed 18 produk idempoten; `.env.example`; `requirements.txt`.
- Test: 26 pytest (auth, produk, order snapshot+stok, troli navigasi/stale).
- `backend/README.md` dokumentasi kontrak API + catatan MySQL vs SQLite.

### Dokumentasi
- `README.md` (root) ditulis ulang.
- `docs/PROJECT_AUDIT.md`, `docs/IMPLEMENTATION_PLAN.md` (sudah ada).
- `docs/HARDWARE_CONTRACT.md` (baru) — batas integrasi hardware.

## Belum diverifikasi / batasan

1. **Login/katalog end-to-end dari emulator** — membutuhkan emulator dan backend
   berjalan bersamaan. Kontrak API sudah dicocokkan dengan response backend
   (ditest via pytest), tetapi lintas-proses Android↔FastAPI belum dijalankan.
2. **MariaDB nyata** — server XAMPP tidak berjalan; test memakai SQLite
   (didokumentasikan sebagai batasan, bukan penggantian diam-diam).
3. **Hardware UWB/ESP32/motor** — tidak terintegrasi; dokumen firmware tidak
   tersedia; adapter hardware hanya placeholder tanpa perintah motor.

## TODO yang tersisa (jika lingkungan memungkinkan)

- Nyalakan MariaDB dan jalankan `pytest` terhadap MySQL nyata.
- Jalankan backend + emulator bersamaan untuk uji login/katalog end-to-end.
- Ganti `HardwareTrolleyNavigationRepository` saat kontrak firmware tersedia.
