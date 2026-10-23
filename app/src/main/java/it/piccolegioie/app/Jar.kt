package it.piccolegioie.app

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun HappinessJar(count: Int, modifier: Modifier = Modifier, fishSignal: Int = 0, onTap: () -> Unit = {}) {
    val infinite = rememberInfiniteTransition(label = "quiet afternoon")
    val breathing by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(3600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "light")
    val drift by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(11000, easing = LinearEasing)), label = "dust in sunlight")
    val shake = remember { Animatable(0f) }
    val fall = remember { Animatable(1f) }
    LaunchedEffect(fishSignal) {
        if (fishSignal > 0) { shake.snapTo(0f); shake.animateTo(1f, tween(850)); shake.snapTo(0f) }
    }
    var lastCount by remember { mutableIntStateOf(count) }
    LaunchedEffect(count) {
        if (count > lastCount) { fall.snapTo(0f); fall.animateTo(1f, tween(850, easing = FastOutSlowInEasing)) }
        lastCount = count
    }
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }
    Canvas(modifier.fillMaxWidth().height(294.dp).joyEntrance(2)
        .semantics { contentDescription = "Barattolo di vetro con $count ricordi. Tocca per pescare un bigliettino." }
        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onTap)) {
        val factor = min(size.width / 360f, size.height / 300f)
        translate((size.width - 360f * factor) / 2, (size.height - 300f * factor) / 2) {
            scale(factor, Offset.Zero) {
                drawCircle(Brush.radialGradient(listOf(Color(0xFFF0DFC0).copy(alpha = .38f + breathing * .08f), Cream.copy(alpha = 0f)), Offset(180f, 155f), 155f), 155f, Offset(180f, 155f))
                repeat(10) { i ->
                    val age = (drift + i * .137f) % 1f
                    val x = 55f + (i * 67 % 255) + sin(age * PI * 2 + i).toFloat() * 10f
                    val y = 274f - age * 245f
                    drawCircle(Gold.copy(alpha = sin(age * PI).toFloat().coerceAtLeast(0f) * .24f), 1f + i % 3 * .45f, Offset(x, y))
                }
                drawOval(Brush.radialGradient(listOf(Color(0xFF6E6851).copy(alpha = .15f), Color.Transparent), Offset(180f, 276f), 130f), Offset(49f, 259f), Size(260f, 33f))
                // A sprig lying on the table.
                drawPath(Path().apply { moveTo(42f, 263f); cubicTo(53f, 249f, 57f, 230f, 66f, 224f) }, Color(0xFF89977A), style = Stroke(1.6f, cap = StrokeCap.Round))
                listOf(Triple(48f, 254f, -35f), Triple(57f, 243f, 40f), Triple(59f, 232f, -35f)).forEach { (x, y, angle) ->
                    rotate(angle, Offset(x, y)) { drawOval(Color(0xFFA3AD8A), Offset(x - 4, y - 10), Size(8f, 17f)) }
                }
                rotate(sin(shake.value * PI * 5).toFloat() * 7f * (1 - shake.value) + sin(breathing * PI * 2).toFloat() * .65f, Offset(180f, 270f)) {
                    // Thick hand-blown glass silhouette.
                    val jar = Path().apply {
                        moveTo(126f, 63f); lineTo(234f, 63f); lineTo(234f, 81f)
                        cubicTo(234f, 96f, 279f, 98f, 281f, 128f)
                        cubicTo(285f, 163f, 287f, 218f, 280f, 247f)
                        cubicTo(277f, 264f, 264f, 273f, 247f, 274f)
                        quadraticTo(180f, 282f, 113f, 274f)
                        cubicTo(96f, 273f, 83f, 264f, 80f, 247f)
                        cubicTo(73f, 218f, 75f, 163f, 79f, 128f)
                        cubicTo(81f, 98f, 126f, 96f, 126f, 81f); close()
                    }
                    drawPath(jar, Brush.horizontalGradient(listOf(Color(0xFFE2EBDF).copy(alpha = .55f), Color.White.copy(alpha = .25f), Color(0xFFDFE9DE).copy(alpha = .38f), Color(0xFFCBDBD2).copy(alpha = .6f)), 75f, 285f))
                    clipPath(jar) {
                        // Folded paper pieces settle inside the jar as it fills.
                        val positions = listOf(Triple(126f, 244f, -19f), Triple(218f, 247f, 16f), Triple(173f, 238f, -8f),
                            Triple(250f, 225f, -29f), Triple(106f, 222f, 26f), Triple(156f, 213f, 12f), Triple(204f, 218f, -23f),
                            Triple(135f, 192f, -18f), Triple(231f, 195f, 22f), Triple(184f, 181f, 8f), Triple(104f, 183f, -18f),
                            Triple(250f, 167f, 11f), Triple(158f, 160f, -24f), Triple(207f, 148f, 20f), Triple(131f, 137f, -12f), Triple(184f, 123f, 12f))
                        positions.take((count - if (fall.value < 1f) 1 else 0).coerceIn(0, 16)).forEachIndexed { i, (x, y, r) ->
                            rotate(r + sin(shake.value * PI * 5).toFloat() * 5f * (1 - shake.value), Offset(x, y)) {
                                val base = PaperColors[i % 5]
                                drawRoundRect(Color(0xFF5D6548).copy(alpha = .1f), Offset(x - 25, y - 14), Size(55f, 36f), CornerRadius(3f))
                                drawRoundRect(Brush.verticalGradient(listOf(base, base.copy(alpha = .8f)), y - 20, y + 15), Offset(x - 28, y - 19), Size(55f, 34f), CornerRadius(2f))
                                drawLine(SoftSurface.copy(alpha = .65f), Offset(x - 26, y - 17), Offset(x + 25, y - 17), 1f)
                                drawPath(Path().apply { moveTo(x - 28, y - 19); lineTo(x, y + 2); lineTo(x + 27, y - 19) }, Ink.copy(alpha = .12f), style = Stroke(.8f))
                            }
                        }
                        if (fall.value < 1f) {
                            val target = positions[(count - 1).coerceIn(0, 15)]
                            val t = fall.value
                            val x = 180f + (target.first - 180f) * t + sin(t * PI * 2).toFloat() * 13f * (1-t)
                            val y = 65f + (target.second - 65f) * t
                            rotate(target.third + 100f * (1 - t), Offset(x, y)) {
                                drawRoundRect(PaperColors[(count - 1).coerceAtLeast(0) % 5], Offset(x - 28, y - 19), Size(55f, 34f), CornerRadius(2f))
                                drawPath(Path().apply { moveTo(x - 28, y - 19); lineTo(x, y + 2); lineTo(x + 27, y - 19) }, Ink.copy(alpha = .15f), style = Stroke(.8f))
                                }
                        }
                        // A wide, soft reflection glides across the curved glass.
                        val lightX = -110f + drift * 560f
                        rotate(16f, Offset(180f, 180f)) {
                            drawRect(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = .27f), Color.Transparent), lightX, lightX + 40), Offset(lightX, 90f), Size(40f, 200f))
                        }
                        drawOval(Color.White.copy(alpha = .25f), Offset(84f, 252f), Size(194f, 21f), style = Stroke(3f))
                    }
                    drawPath(jar, Color(0xFFB2C6B8).copy(alpha = .6f), style = Stroke(1.4f))
                    // Reflections follow the curved edges of the glass.
                    drawPath(Path().apply { moveTo(115f, 101f); cubicTo(90f, 111f, 89f, 119f, 89f, 155f) }, Color.White.copy(alpha = .8f), style = Stroke(5f, cap = StrokeCap.Round))
                    drawLine(Color.White.copy(alpha = .6f), Offset(88f, 165f), Offset(88f, 202f), 4f, StrokeCap.Round)
                    drawPath(Path().apply { moveTo(266f, 134f); quadraticTo(274f, 192f, 267f, 230f) }, Color.White.copy(alpha = .48f), style = Stroke(3f, cap = StrokeCap.Round))
                    // Warm paper label.
                    drawRoundRect(Color(0xFF647357).copy(alpha = .12f), Offset(122f, 164f), Size(118f, 61f), CornerRadius(5f))
                    drawRoundRect(Color(0xFFFFFCF2), Offset(121f, 162f), Size(118f, 60f), CornerRadius(5f))
                    drawRoundRect(Color(0xFFD9CFB2), Offset(126f, 167f), Size(108f, 50f), CornerRadius(3f), style = Stroke(.65f))
                    drawContext.canvas.nativeCanvas.apply {
                        paint.color = android.graphics.Color.rgb(76, 91, 66)
                        paint.textSize = 20f; paint.textAlign = Paint.Align.CENTER
                        paint.typeface = Typeface.create("serif", Typeface.ITALIC)
                        drawText("cose belle", 180f, 193f, paint)
                        paint.textSize = 6.8f; paint.letterSpacing = .22f
                        paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                        drawText("DA TENERE VICINE", 180f, 207f, paint); paint.letterSpacing = 0f
                    }
                    // Cork lid with warm grain and a soft upper highlight.
                    drawRoundRect(Color(0xFF7A5A36).copy(alpha = .15f), Offset(123f, 58f), Size(115f, 18f), CornerRadius(4f))
                    translate(0f, -sin(shake.value * PI).toFloat() * 14f) {
                    rotate(sin(shake.value * PI * 2).toFloat() * 4f, Offset(180f, 55f)) {
                    drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFD8BE8E), Color(0xFFB59461), Color(0xFFC5A677)), 43f, 68f), Offset(121f, 43f), Size(118f, 26f), CornerRadius(6f))
                    drawOval(Color(0xFFE6D0A6), Offset(122f, 38f), Size(116f, 14f))
                    repeat(34) { i ->
                        val x = 127f + ((i * 31) % 105)
                        val y = 50f + ((i * 7) % 16)
                        drawLine(Color(0xFF9A784B).copy(alpha = .3f), Offset(x, y), Offset(x + 1.8f, y + .5f), .8f)
                    }
                    }
                    }
                    drawRoundRect(Color(0xFFD9D6BB), Offset(123f, 75f), Size(114f, 4f), CornerRadius(2f))
                    drawLine(Color(0xFFA89570), Offset(126f, 78f), Offset(233f, 78f), 1f)
                    drawPath(Path().apply {
                        moveTo(210f, 78f); cubicTo(222f, 58f, 238f, 70f, 214f, 80f)
                        cubicTo(194f, 64f, 189f, 81f, 210f, 80f)
                        quadraticTo(207f, 97f, 202f, 102f); moveTo(214f, 80f); quadraticTo(221f, 94f, 219f, 105f)
                    }, Color(0xFFA38C60), style = Stroke(1.5f, cap = StrokeCap.Round))
                }
                fun sparkle(x: Float, y: Float, r: Float) {
                    drawPath(Path().apply { moveTo(x, y-r); quadraticTo(x+1,y-1,x+r,y); quadraticTo(x+1,y+1,x,y+r); quadraticTo(x-1,y+1,x-r,y); quadraticTo(x-1,y-1,x,y-r) }, Gold.copy(alpha = .6f + breathing * .2f))
                }
                sparkle(66f, 98f - breathing * 3, 6f); sparkle(292f, 66f + breathing * 4, 9f)
                sparkle(302f, 192f - breathing * 3, 4f)
                drawCircle(Gold.copy(alpha = .5f), 1.5f, Offset(82f, 62f + breathing * 3))
                drawCircle(Gold.copy(alpha = .4f), 2f, Offset(282f, 94f))
            }
        }
    }
}
