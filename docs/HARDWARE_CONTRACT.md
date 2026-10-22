# Kontrak Hardware Troli (Belum Terverifikasi)

Dokumen ini menjelaskan batas integrasi antara aplikasi dan subsistem troli
fisik, serta mengapa aplikasi saat ini **tidak** mengirim perintah motor.

## Status

- Dokumen firmware yang dirujuk master prompt (`ESP32 UWB Positioning`,
  `Smart Trolley Autonomous Navigation System`) **tidak tersedia** di repository.
- Satu-satunya dokumen yang ada adalah makalah historis 2019 (IOIO/Navisens)
  yang **tidak** menyebut ESP32-S3, UWB, maupun Dijkstra.
- Oleh karena itu **tidak ada klaim** tentang format telemetri, protokol motor,
  maupun topik MQTT/ROS2 yang dibuat. Semua integrasi hardware ditandai
  "belum terverifikasi".

## Kontrak navigasi & positioning (terverifikasi dari repo)

Dokumen firmware kini tersedia di dua repo integrasi:

- **Positioning (UWB)**: `wideband-positioning` (server Flask). Kontrak beku
  `docs/API.md` — `GET /api/v1/state` mengembalikan `room {width, depth,
  height}` dan `tags[]` (`id`, `x`, `y`, `confidence`, `ambiguous`, `online`,
  `last_seen`). `online` bermakna segar (`< 5 s`), bukan "kualitas posisi".
- **Navigasi**: `wideband-navigation`. Telemetri status (5 Hz) di
  `docs/data_contract.md` memiliki `navigation.state ∈ {BOOT, IDLE,
  WAITING_FOR_POSITION, READY, PLANNING, NAVIGATING, REPLANNING,
  POSITION_LOST, ARRIVED, ERROR, EMERGENCY_STOP}` dan `navigation.error`.
  Kerangka koordinat tunggal `smart_market_map` (origin kiri-bawah, +X kanan,
  +Y atas, satuan meter).

Aplikasi Android **menampilkan** status ini (termasuk status baru
`POSITION_LOST` di `TrolleyStatus`) tetapi **tidak** menjalankan Dijkstra/PID
atau mengirim perintah motor — tanggung jawab tersebut tetap di navigation core
ESP32-S3. Tujuan navigasi dikirim melalui kontrak backend Smart Market
(`POST /api/v1/trolleys/{id}/navigation`, app-side), bukan langsung ke ESP32.

## Seam

Aplikasi berkomunikasi dengan troli hanya melalui satu interface:

```kotlin
interface TrolleyNavigationRepository {
    val state: StateFlow<TrolleyState>
    fun enterDemoMode()
    fun startNavigation(route: List<String>, targetNodeId: String)
    fun step()
    fun pause()
    fun resume()
    fun reset()
    fun cancel()
    fun clearFault()
}
```

## Implementasi

| Kelas | `TrolleySource` | Perilaku |
|---|---|---|
| `DemoTrolleyNavigationRepository` | `DEMO` | Memajukan marker in-memory satu node per `step()`; label UI "Simulasi". |
| `HardwareTrolleyNavigationRepository` | `HARDWARE` | Placeholder; **tidak** mengirim perintah motor, hanya mencatat intent di `state.message`. |

## Aturan

1. `TrolleyNavigationRepository` **MUST NOT** mengirim perintah motor sampai
   protokol firmware diverifikasi.
2. Demo state **MUST NOT** pernah ditampilkan sebagai status hardware nyata
   (enum `TrolleySource` membedakannya).
3. Saat firmware tersedia, implementasi hardware menggantikan placeholder tanpa
   mengubah UI — cukup mengganti wiring di `AppContainer`.

## Target integrasi (belum diimplementasikan)

Ketika kontrak nyata tersedia, adapter hardware akan memetakan:

- `startNavigation(route, target)` → perintah tujuan firmware.
- telemetri masuk → update `state` (`currentNodeId`, `status`, `lastUpdateMs`).
- `step()`/`pause()`/`resume()`/`cancel()` → perintah kontrol sesuai protokol.

Format pastinya menunggu dokumen firmware; tidak diinvensi.
