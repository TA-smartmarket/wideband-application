package com.smartmarket.kiosk.data.uwb

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Whether the position origin is real UWB hardware or none (offline/demo). */
enum class UwbSource { NONE, REAL }

/**
 * Snapshot of the configured trolley tag's position and connection quality, as
 * reported by the positioning server (`GET /api/v1/state`). Coordinates are
 * metres in the room frame (origin bottom-left, +X right, +Y up — as per the
 * positioning server contract). `NONE` means "no real position yet".
 */
data class UwbState(
    val connected: Boolean = false,
    val tagOnline: Boolean = false,
    val x: Float? = null,
    val y: Float? = null,
    val confidence: Float? = null,
    val ambiguous: Boolean = false,
    val lastSeenM: Long = 0L,
    val source: UwbSource = UwbSource.NONE,
    val stale: Boolean = false,
    val error: String? = null,
    val roomWidthM: Float = 0f,
    val roomHeightM: Float = 0f,
)

/**
 * Polls the UWB positioning server at a fixed interval and exposes the latest
 * tag state. It never invents coordinates: when the server/tag is unavailable,
 * [UwbState] reports disconnected/offline with a clear error label and existing
 * positions are marked stale instead of being presented as live.
 *
 * Polling is lifecycle-managed by the host ([start]/[pause]/[stop]) so it stops
 * when the screen is not visible and resumes afterwards.
 */
class UwbPositioningRepository(
    private val baseUrl: String,
    private val tagId: String = "tag-1",
    private val staleAfterMs: Long = 3_000L,
    private val pollIntervalMs: Long = 1_000L,
    client: OkHttpClient = defaultClient(),
) {

    private val client: OkHttpClient = client

    private val _state = MutableStateFlow(UwbState())
    val state: StateFlow<UwbState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollJob: Job? = null

    /** Monotonic timestamp of the last VALID position; used for staleness only. */
    @Volatile
    private var lastGoodElapsedMs: Long = 0L

    fun start() {
        if (pollJob?.isActive == true) return
        pollJob = scope.launch {
            while (isActive) {
                pollOnce()
                delay(pollIntervalMs)
            }
        }
    }

    /** Stops the poll loop but keeps the last state (for app backgrounding). */
    fun pause() {
        pollJob?.cancel()
        pollJob = null
    }

    /** Stops polling and clears any previously shown position. */
    fun stop() {
        pause()
        _state.value = UwbState()
    }

    /** Performs a single poll; idempotent and safe to call repeatedly. */
    fun pollOnce() {
        val url = baseUrl.trimEnd('/') + "/api/v1/state"
        val request = Request.Builder().url(url).get().build()
        try {
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    updateDisconnected("Server UWB: HTTP ${resp.code}")
                    return
                }
                val body = resp.body?.string()
                if (body.isNullOrBlank()) {
                    updateDisconnected("Respons UWB kosong")
                    return
                }
                applyStateJson(body)
            }
        } catch (e: IOException) {
            updateDisconnected("Tidak terhubung ke server UWB")
        } catch (e: Exception) {
            updateDisconnected("Respons UWB tidak valid")
        }
    }

    private fun applyStateJson(body: String) {
        val root = JSONObject(body)
        val room = root.optJSONObject("room")
        val roomW = room?.optDouble("width")?.toFloat() ?: 0f
        val roomH = room?.optDouble("height")?.toFloat() ?: 0f

        val tag = findTag(root)
        if (tag == null) {
            _state.value = UwbState(
                connected = true,
                tagOnline = false,
                source = UwbSource.REAL,
                stale = isStale(),
                roomWidthM = roomW,
                roomHeightM = roomH,
            )
            return
        }

        val online = tag.optBoolean("online", false)
        val x = optFloat(tag, "x")
        val y = optFloat(tag, "y")
        val confidence = optFloat(tag, "confidence")
        val ambiguous = tag.optBoolean("ambiguous", false)
        val lastSeen = tag.optLong("last_seen", 0L)

        if (online && x != null && y != null) {
            lastGoodElapsedMs = SystemClock.elapsedRealtime()
            _state.value = UwbState(
                connected = true,
                tagOnline = true,
                x = x,
                y = y,
                confidence = confidence,
                ambiguous = ambiguous,
                lastSeenM = lastSeen,
                source = UwbSource.REAL,
                stale = false,
                error = null,
                roomWidthM = roomW,
                roomHeightM = roomH,
            )
            return
        }

        // Server reachable but this tag has no usable position yet.
        val hadData = _state.value.x != null
        _state.value = UwbState(
            connected = true,
            tagOnline = online,
            source = if (online || hadData) UwbSource.REAL else UwbSource.NONE,
            stale = hadData && isStale(),
            error = if (online) "Posisi tag belum tersedia" else "Tag UWB offline",
            roomWidthM = roomW,
            roomHeightM = roomH,
        )
    }

    private fun findTag(root: JSONObject): JSONObject? {
        val tags = root.optJSONArray("tags") ?: return null
        for (i in 0 until tags.length()) {
            val t = tags.optJSONObject(i) ?: continue
            if (t.optString("id") == tagId) return t
        }
        return null
    }

    private fun updateDisconnected(message: String) {
        val prev = _state.value
        val hadData = prev.x != null
        _state.value = prev.copy(
            connected = false,
            tagOnline = false,
            source = if (hadData) UwbSource.REAL else UwbSource.NONE,
            stale = hadData && isStale(),
            error = message,
        )
    }

    private fun isStale(): Boolean {
        if (lastGoodElapsedMs == 0L) return false
        return SystemClock.elapsedRealtime() - lastGoodElapsedMs > staleAfterMs
    }

    private fun optFloat(obj: JSONObject, key: String): Float? =
        if (obj.has(key) && !obj.isNull(key)) obj.optDouble(key).toFloat() else null

    companion object {
        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .build()
    }
}