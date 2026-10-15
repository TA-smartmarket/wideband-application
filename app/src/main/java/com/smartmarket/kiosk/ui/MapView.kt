package com.smartmarket.kiosk.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.smartmarket.kiosk.R
import com.smartmarket.kiosk.data.model.MapGraph
import com.smartmarket.kiosk.data.model.Node
import com.smartmarket.kiosk.data.model.Route
import com.smartmarket.kiosk.data.model.Shelf

/**
 * XML-declarable canvas view that draws the supermarket floor plan, the demo
 * graph network, a resolved route, and the trolley marker.
 *
 * Coordinate model: logical units (0..1000 x 0..1000) from [MapGraph]. The view
 * maps them onto its own size; the Y axis is flipped so map "up" points up.
 * Marker colour and legend are resolved here so the fragment stays declarative.
 */
class MapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var graph: MapGraph? = null
    private var route: Route? = null

    // Simulated trolley position (null = no demo navigation active).
    private var simX: Float? = null
    private var simY: Float? = null

    // Real UWB position (null = not available).
    private var realX: Float? = null
    private var realY: Float? = null

    // Positioning room dimensions in metres (drives real→logical mapping).
    private var roomW = 0f
    private var roomH = 0f

    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(3f)
        color = color(R.color.sm_green)
        strokeCap = Paint.Cap.ROUND
    }
    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.5f)
        color = color(R.color.sm_divider)
    }
    private val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = color(R.color.sm_muted)
    }
    private val shelfPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = color(R.color.sm_green_light)
    }
    private val shelfStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
        color = color(R.color.sm_green)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(11f)
        color = color(R.color.sm_green_dark)
        textAlign = Paint.Align.CENTER
    }
    private val nodeLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(10f)
        color = color(R.color.sm_muted)
        textAlign = Paint.Align.CENTER
    }
    private val destinationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = color(R.color.sm_green_dark)
    }
    private val trolleyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = color(R.color.sm_uwb)
    }
    private val trolleyRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
        color = color(R.color.sm_uwb)
    }
    private val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
        color = color(R.color.sm_muted)
    }
    private val textBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(200, 255, 255, 255)
    }

    private val shelfRect = RectF()
    private val windowRect = RectF()
    private val invRect = RectF()

    // Shared transform state computed once per draw.
    private var scale = 1f
    private var padPx = 0f

    init {
        setBackgroundColor(color(R.color.sm_bg))
    }

    fun setGraph(value: MapGraph) {
        graph = value
        invalidate()
    }

    fun setRoute(value: Route?) {
        route = value
        invalidate()
    }

    /** Simulated trolley location (demo navigation). */
    fun setSimPosition(x: Float, y: Float) {
        simX = x
        simY = y
        invalidate()
    }

    fun clearSimPosition() {
        simX = null
        simY = null
        invalidate()
    }

    /** Real UWB position (metres in the room frame). */
    fun setRealPosition(x: Float?, y: Float?) {
        realX = x
        realY = y
        invalidate()
    }

    /** Positioning room dimensions in metres; drives real→logical mapping. */
    fun setRoomSize(widthM: Float, heightM: Float) {
        roomW = widthM
        roomH = heightM
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val g = graph ?: run {
            drawPlaceholder(canvas)
            return
        }

        drawShelves(canvas, g)
        drawEdges(canvas, g)
        drawRoute(canvas, g)
        drawNodes(canvas, g)
        drawDestinations(canvas, g)
        drawStart(canvas, g)
        drawTrolley(canvas, g)
    }

    private fun updateTransform() {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        // Logical map is square (0..1000); fit to the narrower axis, centred.
        padPx = dp(24f)
        val contentW = (w - 2 * padPx).coerceAtLeast(1f)
        val contentH = (h - 2 * padPx).coerceAtLeast(1f)
        val min = minOf(contentW, contentH)
        scale = min / 1000f
    }

    private fun mapX(x: Float): Float {
        if (scale <= 0f) updateTransform()
        return x * scale + padPx
    }

    private fun mapY(y: Float): Float {
        if (scale <= 0f) updateTransform()
        // Demo floor plan uses screen-like coords (Y down); draw directly.
        return y * scale + padPx
    }

    private fun drawPlaceholder(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = sp(14f)
            color = color(R.color.sm_muted)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Denah tidak tersedia",
            width / 2f,
            height / 2f,
            paint,
        )
    }

    private fun drawShelves(canvas: Canvas, g: MapGraph) {
        shelfRect.setEmpty()
        for (s in g.shelves) {
            drawShelf(canvas, s)
        }
    }

    private fun drawShelf(canvas: Canvas, s: Shelf) {
        val left = mapX(s.left)
        val top = mapY(s.top)
        val right = mapX(s.right)
        val bottom = mapY(s.bottom)
        shelfRect.set(left, top, right, bottom)
        canvas.drawRect(shelfRect, shelfPaint)
        canvas.drawRect(shelfRect, shelfStrokePaint)
        val cx = shelfRect.centerX()
        val cy = shelfRect.centerY()
        canvas.drawText(s.label, cx, cy - (labelPaint.ascent() + labelPaint.descent()) / 2f, labelPaint)
    }

    private fun drawEdges(canvas: Canvas, g: MapGraph) {
        for (e in g.edges) {
            val a = g.nodeById[e.from] ?: continue
            val b = g.nodeById[e.to] ?: continue
            canvas.drawLine(mapX(a.x), mapY(a.y), mapX(b.x), mapY(b.y), edgePaint)
        }
    }

    private fun drawRoute(canvas: Canvas, g: MapGraph) {
        val r = route ?: return
        val nodes = r.nodeIds.mapNotNull { g.nodeById[it] }
        if (nodes.size < 2) return
        for (i in 0 until nodes.lastIndex) {
            val a = nodes[i]
            val b = nodes[i + 1]
            canvas.drawLine(mapX(a.x), mapY(a.y), mapX(b.x), mapY(b.y), pathPaint)
        }
    }

    private fun drawNodes(canvas: Canvas, g: MapGraph) {
        for (n in g.nodes) {
            if (n.id == g.startNodeId) continue
            drawNode(canvas, n)
        }
    }

    private fun drawNode(canvas: Canvas, n: Node) {
        val x = mapX(n.x)
        val y = mapY(n.y)
        canvas.drawCircle(x, y, dp(4f), nodePaint)
    }

    private fun drawStart(canvas: Canvas, g: MapGraph) {
        val start = g.nodeById[g.startNodeId] ?: return
        val x = mapX(start.x)
        val y = mapY(start.y)
        val r = dp(7f)
        canvas.drawCircle(x, y, r, textBgPaint)
        canvas.drawCircle(x, y, r, startPaint)
        canvas.drawCircle(x, y, dp(3f), nodePaint)
    }

    private fun drawDestinations(canvas: Canvas, g: MapGraph) {
        for (d in g.destinations) {
            val n = g.nodeById[d.nodeId] ?: continue
            if (n.id == g.startNodeId) continue
            val x = mapX(n.x)
            val y = mapY(n.y)
            canvas.drawCircle(x, y, dp(6f), destinationPaint)
        }
    }

    private fun drawTrolley(canvas: Canvas, g: MapGraph) {
        // Prefer real UWB position; otherwise fall back to simulated marker.
        val markerX = realX?.let { roomToLogicalX(it, g) } ?: simX
        val markerY = realY?.let { roomToLogicalY(it, g) } ?: simY
        val ux = markerX ?: g.nodeById[g.startNodeId]?.x ?: return
        val uy = markerY ?: g.nodeById[g.startNodeId]?.y ?: return

        val px = mapX(ux)
        val py = mapY(uy)
        val radius = dp(9f)
        canvas.drawCircle(px, py, radius + dp(4f), trolleyRingPaint)
        canvas.drawCircle(px, py, radius, trolleyPaint)
    }

    /** Room coords are metres (0..width, 0..height) — map proportionally. */
    private fun roomToLogicalX(m: Float, g: MapGraph): Float {
        val roomW = if (this.roomW > 0f) this.roomW else largestRoomDimension(g).first
        return if (roomW > 0f) (m / roomW) * 1000f else 500f
    }

    private fun roomToLogicalY(m: Float, g: MapGraph): Float {
        val roomH = if (this.roomH > 0f) this.roomH else largestRoomDimension(g).second
        // Room origin is bottom-left with +Y up; demo coords are Y-down, so invert.
        return if (roomH > 0f) 1000f - (m / roomH) * 1000f else 500f
    }

    private fun largestRoomDimension(g: MapGraph): Pair<Float, Float> {
        // The room is the bounding box of shelves padded by 20% on each side.
        var left = Float.MAX_VALUE
        var top = Float.MAX_VALUE
        var right = Float.MIN_VALUE
        var bottom = Float.MIN_VALUE
        for (s in g.shelves) {
            left = minOf(left, s.left)
            top = minOf(top, s.top)
            right = maxOf(right, s.right)
            bottom = maxOf(bottom, s.bottom)
        }
        if (left == Float.MAX_VALUE) return 0f to 0f
        val w = right - left
        val h = bottom - top
        return w * 1.2f to h * 1.2f
    }

    private fun color(res: Int) = ContextCompat.getColor(context, res)

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density

    private fun sp(value: Float): Float =
        value * resources.displayMetrics.scaledDensity
}