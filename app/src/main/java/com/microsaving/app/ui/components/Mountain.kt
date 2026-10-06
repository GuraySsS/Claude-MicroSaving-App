package com.microsaving.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.hypot
import kotlin.random.Random

/** Peak of the main mountain in normalized (0..1) coordinates. */
private const val PEAK_X = 0.5f
private const val PEAK_Y = 0.13f
private const val BASE_HALF_WIDTH = 0.47f

/** Switchback trail from the foot of the mountain to the summit, in normalized coordinates. */
private val trail = listOf(
    Offset(0.22f, 0.96f),
    Offset(0.72f, 0.87f),
    Offset(0.30f, 0.75f),
    Offset(0.66f, 0.63f),
    Offset(0.38f, 0.51f),
    Offset(0.60f, 0.40f),
    Offset(0.45f, 0.29f),
    Offset(0.52f, 0.20f),
    Offset(PEAK_X, PEAK_Y + 0.01f),
)

private val segmentLengths = trail.zipWithNext { a, b -> hypot(b.x - a.x, b.y - a.y) }
private val trailLength = segmentLengths.sum()

/** Point on the trail at [t] (0 = base, 1 = summit), measured along the trail length. */
private fun trailPoint(t: Float): Offset {
    var remaining = t.coerceIn(0f, 1f) * trailLength
    for (i in segmentLengths.indices) {
        val len = segmentLengths[i]
        if (remaining <= len) {
            val f = if (len == 0f) 0f else remaining / len
            val a = trail[i]
            val b = trail[i + 1]
            return Offset(a.x + (b.x - a.x) * f, a.y + (b.y - a.y) * f)
        }
        remaining -= len
    }
    return trail.last()
}

/** Half width of the mountain at height [y]; used to keep trees on the slopes. */
private fun halfWidthAt(y: Float): Float = BASE_HALF_WIDTH * ((y - PEAK_Y) / (1f - PEAK_Y)).coerceAtLeast(0f)

/**
 * Forest-style illustration: every day under budget plants a tree on the mountain and
 * the climber walks up the trail as savings grow towards the goal at the summit.
 *
 * @param progress saved / target, 0..1
 * @param projectedProgress where the climber would be if today ended now (shown as a ghost)
 * @param trees number of successful days
 */
@Composable
fun MountainScene(
    progress: Float,
    projectedProgress: Float,
    trees: Int,
    goalEmoji: String,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(progress, tween(1400), label = "progress")
    val animatedGhost by animateFloatAsState(projectedProgress, tween(900), label = "ghost")
    val infinite = rememberInfiniteTransition(label = "sky")
    val cloudShift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(40_000, easing = LinearEasing)),
        label = "clouds",
    )
    val bob by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "bob",
    )

    Canvas(modifier) {
        drawSky(cloudShift)
        drawBackRange()
        drawMainMountain()
        drawTrees(trees)
        drawTrail(animatedProgress)
        drawCamps(animatedProgress)
        drawSummitFlag(goalEmoji, reached = animatedProgress >= 0.999f)
        if (animatedGhost > animatedProgress + 0.002f) {
            drawEmoji("🧗", trailPoint(animatedGhost).px(size), size.minDimension * 0.08f, alpha = 0.35f)
        }
        val climber = trailPoint(animatedProgress).px(size)
        drawEmoji(
            if (animatedProgress >= 0.999f) "🙌" else "🧗",
            climber.copy(y = climber.y - bob * size.minDimension * 0.012f),
            size.minDimension * 0.1f,
        )
    }
}

private fun Offset.px(size: Size) = Offset(x * size.width, y * size.height)

private fun DrawScope.drawSky(cloudShift: Float) {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF81D4FA), Color(0xFFB3E5FC), Color(0xFFFFF3E0))))
    drawCircle(Color(0xFFFFF59D), radius = size.minDimension * 0.08f, center = Offset(size.width * 0.84f, size.height * 0.14f))
    drawCircle(Color(0x55FFF59D), radius = size.minDimension * 0.13f, center = Offset(size.width * 0.84f, size.height * 0.14f))
    val clouds = listOf(Offset(0.1f, 0.12f) to 1f, Offset(0.55f, 0.07f) to 0.75f, Offset(0.85f, 0.3f) to 0.6f)
    clouds.forEach { (pos, scale) ->
        val travel = (pos.x + cloudShift * (0.6f + scale * 0.4f)) % 1.3f - 0.15f
        drawCloud(Offset(travel * size.width, pos.y * size.height), size.minDimension * 0.05f * scale)
    }
}

private fun DrawScope.drawCloud(center: Offset, r: Float) {
    val c = Color.White.copy(alpha = 0.9f)
    drawCircle(c, r, center)
    drawCircle(c, r * 0.8f, center + Offset(-r * 1.1f, r * 0.25f))
    drawCircle(c, r * 0.85f, center + Offset(r * 1.1f, r * 0.2f))
    drawCircle(c, r * 0.7f, center + Offset(r * 0.3f, -r * 0.5f))
}

private fun DrawScope.drawBackRange() {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(0f, h * 0.55f)
        lineTo(w * 0.12f, h * 0.38f)
        lineTo(w * 0.24f, h * 0.5f)
        lineTo(w * 0.33f, h * 0.42f)
        lineTo(w * 0.5f, h * 0.7f)
        lineTo(w * 0.68f, h * 0.36f)
        lineTo(w * 0.8f, h * 0.48f)
        lineTo(w * 0.9f, h * 0.33f)
        lineTo(w, h * 0.45f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(path, Brush.verticalGradient(listOf(Color(0xFFB39DDB), Color(0xFF9FA8DA))))
}

private fun DrawScope.drawMainMountain() {
    val w = size.width
    val h = size.height
    val left = PEAK_X - BASE_HALF_WIDTH
    val right = PEAK_X + BASE_HALF_WIDTH
    val mountain = Path().apply {
        moveTo(left * w, h)
        lineTo(PEAK_X * w, PEAK_Y * h)
        lineTo(right * w, h)
        close()
    }
    drawPath(
        mountain,
        Brush.verticalGradient(
            0f to Color(0xFF8D9DA6),
            0.35f to Color(0xFF7CB342),
            1f to Color(0xFF388E3C),
            startY = PEAK_Y * h,
            endY = h,
        ),
    )
    // Shaded right face gives the mountain some depth.
    val shade = Path().apply {
        moveTo(PEAK_X * w, PEAK_Y * h)
        lineTo(right * w, h)
        lineTo((PEAK_X + 0.12f) * w, h)
        close()
    }
    drawPath(shade, Color.Black.copy(alpha = 0.08f))
    // Snow cap.
    val capY = PEAK_Y + 0.12f
    val capHalf = halfWidthAt(capY)
    val snow = Path().apply {
        moveTo(PEAK_X * w, PEAK_Y * h)
        lineTo((PEAK_X + capHalf) * w, capY * h)
        lineTo((PEAK_X + capHalf * 0.4f) * w, (capY - 0.025f) * h)
        lineTo((PEAK_X) * w, (capY + 0.01f) * h)
        lineTo((PEAK_X - capHalf * 0.45f) * w, (capY - 0.03f) * h)
        lineTo((PEAK_X - capHalf) * w, capY * h)
        close()
    }
    drawPath(snow, Color.White)
    // Foreground meadow.
    drawRect(Color(0xFF2E7D32), topLeft = Offset(0f, h * 0.985f), size = Size(w, h * 0.015f))
}

private fun DrawScope.drawTrees(count: Int) {
    val random = Random(7)
    val shown = count.coerceAtMost(80)
    repeat(shown) {
        val y = random.nextFloat() * 0.42f + 0.55f
        val hw = halfWidthAt(y) * 0.9f
        val x = PEAK_X + (random.nextFloat() * 2f - 1f) * hw
        drawPine(Offset(x * size.width, y * size.height), size.minDimension * (0.035f + y * 0.02f))
    }
}

private fun DrawScope.drawPine(base: Offset, height: Float) {
    val half = height * 0.32f
    drawLine(Color(0xFF5D4037), base, base.copy(y = base.y - height * 0.25f), strokeWidth = height * 0.12f)
    val leaves = Path().apply {
        moveTo(base.x, base.y - height)
        lineTo(base.x + half, base.y - height * 0.2f)
        lineTo(base.x - half, base.y - height * 0.2f)
        close()
    }
    drawPath(leaves, Color(0xFF1B5E20))
    val highlight = Path().apply {
        moveTo(base.x, base.y - height)
        lineTo(base.x - half, base.y - height * 0.2f)
        lineTo(base.x - half * 0.2f, base.y - height * 0.2f)
        close()
    }
    drawPath(highlight, Color(0xFF43A047))
}

private fun DrawScope.drawTrail(progress: Float) {
    val full = Path()
    trail.forEachIndexed { i, p ->
        val px = p.px(size)
        if (i == 0) full.moveTo(px.x, px.y) else full.lineTo(px.x, px.y)
    }
    val stroke = size.minDimension * 0.012f
    drawPath(
        full,
        Color(0xFFFFF8E1).copy(alpha = 0.8f),
        style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 1.5f, stroke * 1.5f))),
    )

    // Walked part of the trail.
    val walked = Path()
    val start = trail.first().px(size)
    walked.moveTo(start.x, start.y)
    var distance = 0f
    val goal = progress * trailLength
    for (i in segmentLengths.indices) {
        if (distance + segmentLengths[i] < goal) {
            val p = trail[i + 1].px(size)
            walked.lineTo(p.x, p.y)
            distance += segmentLengths[i]
        } else {
            val p = trailPoint(progress).px(size)
            walked.lineTo(p.x, p.y)
            break
        }
    }
    drawPath(walked, Color(0xFFFF7043), style = Stroke(stroke * 1.3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawCamps(progress: Float) {
    listOf(0.25f, 0.5f, 0.75f).forEach { at ->
        val p = trailPoint(at).px(size)
        val s = size.minDimension * 0.03f
        val reached = progress >= at
        val tent = Path().apply {
            moveTo(p.x, p.y - s * 1.2f)
            lineTo(p.x + s, p.y)
            lineTo(p.x - s, p.y)
            close()
        }
        drawPath(tent, if (reached) Color(0xFFFFB300) else Color(0xFFCFD8DC))
        drawLine(Color(0xFF5D4037), Offset(p.x, p.y - s * 1.2f), Offset(p.x, p.y), strokeWidth = s * 0.12f)
    }
}

private fun DrawScope.drawSummitFlag(emoji: String, reached: Boolean) {
    val top = Offset(PEAK_X * size.width, PEAK_Y * size.height)
    val poleH = size.minDimension * 0.12f
    drawLine(Color(0xFF5D4037), top, top.copy(y = top.y - poleH), strokeWidth = size.minDimension * 0.008f)
    val flag = Path().apply {
        moveTo(top.x, top.y - poleH)
        lineTo(top.x + poleH * 0.55f, top.y - poleH * 0.8f)
        lineTo(top.x, top.y - poleH * 0.6f)
        close()
    }
    drawPath(flag, if (reached) Color(0xFFFFD600) else Color(0xFFFF7043))
    // The goal sits next to the flag, waiting at the top.
    drawEmoji(emoji, Offset(top.x - poleH * 0.45f, top.y - poleH * 0.35f), size.minDimension * 0.075f)
}

private fun DrawScope.drawEmoji(emoji: String, center: Offset, textSize: Float, alpha: Float = 1f) {
    val paint = android.graphics.Paint().apply {
        this.textSize = textSize
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        this.alpha = (alpha * 255).toInt()
    }
    // Draw so the bottom of the glyph sits on the given point.
    drawContext.canvas.nativeCanvas.drawText(emoji, center.x, center.y - paint.descent(), paint)
}
