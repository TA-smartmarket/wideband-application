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
