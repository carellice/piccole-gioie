package it.piccolegioie.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

/** Read animated values in the layer/draw phases, keeping text layout out of each frame. */
fun Modifier.joyEntrance(order: Int = 0): Modifier = composed {
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((order.coerceIn(0, 8) * 55).toLong())
        reveal.animateTo(1f, spring(dampingRatio = .83f, stiffness = 150f))
    }
    graphicsLayer {
        alpha = reveal.value.coerceIn(0f, 1f)
        translationY = (1f - reveal.value) * 26.dp.toPx()
        scaleX = .97f + .03f * reveal.value
        scaleY = .97f + .03f * reveal.value
    }
}

fun Modifier.joyPress(interaction: MutableInteractionSource): Modifier = composed {
    val pressed by interaction.collectIsPressedAsState()
    val scale = animateFloatAsState(if (pressed) .955f else 1f,
        spring(dampingRatio = .48f, stiffness = 520f), label = "tactile press")
    graphicsLayer { scaleX = scale.value; scaleY = scale.value }
}

fun Modifier.joyUnfold(key: Any): Modifier = composed {
    val unfold = remember { Animatable(0f) }
    LaunchedEffect(key) { unfold.snapTo(0f); unfold.animateTo(1f, spring(dampingRatio = .77f, stiffness = 130f)) }
    graphicsLayer {
        transformOrigin = TransformOrigin(.5f, 0f)
        alpha = (unfold.value * 2).coerceIn(0f, 1f)
        rotationX = -32f * (1f - unfold.value)
        rotationZ = -3.5f * (1f - unfold.value)
        translationY = 26.dp.toPx() * (1 - unfold.value)
        scaleX = .92f + .08f * unfold.value
        scaleY = .88f + .12f * unfold.value
        cameraDistance = 14 * density
    }
}

@Composable
fun AnimatedCount(value: Int, style: TextStyle, modifier: Modifier = Modifier) {
    AnimatedContent(value, modifier, transitionSpec = {
        (fadeIn(tween(240)) + slideInVertically { it / 2 }) togetherWith
            (fadeOut(tween(140)) + slideOutVertically { -it / 2 })
    }, label = "collected moments") { Text("$it", style = style) }
}

@Composable
fun JoyFavorite(favorite: Boolean, onClick: () -> Unit) {
    val beat = remember { Animatable(0f) }
    var touched by remember { mutableStateOf(false) }
    LaunchedEffect(favorite) {
        if (touched) { beat.snapTo(0f); beat.animateTo(1f, tween(650, easing = LinearEasing)) }
    }
    TextButton({ touched = true; onClick() }) {
        Box {
            Icon(if (favorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                if (favorite) "Togli dai preferiti" else "Aggiungi ai preferiti",
                Modifier.size(21.dp).graphicsLayer {
                    val pulse = sin(beat.value * PI).toFloat()
                    scaleX = 1 + pulse * .32f; scaleY = scaleX
                    rotationZ = sin(beat.value * PI * 2).toFloat() * 9
                }, if (favorite) Gold else Forest)
            Canvas(Modifier.matchParentSize()) {
                val t = beat.value
                if (t > 0 && t < 1 && favorite) repeat(8) { index ->
                    val angle = index * PI / 4
                    val radius = size.width * (.6f + t * .75f)
                    drawCircle(Gold.copy(alpha = 1 - t), (2.4f * (1 - t)).dp.toPx(),
                        center + Offset(cos(angle).toFloat() * radius, sin(angle).toFloat() * radius))
                }
            }
        }
        Spacer(Modifier.width(9.dp))
        Text(if (favorite) "Tra i ricordi del cuore" else "Tienilo nel cuore", fontSize = 12.sp)
    }
}

@Composable
fun JoyCelebration(trigger: Int, modifier: Modifier = Modifier) {
    val burst = remember { Animatable(1f) }
    LaunchedEffect(trigger) {
        if (trigger > 0) { burst.snapTo(0f); burst.animateTo(1f, tween(1600, easing = LinearEasing)) }
    }
    Canvas(modifier) {
        val t = burst.value
        if (t <= 0f || t >= 1f) return@Canvas
        val origin = Offset(size.width * .5f, size.height * .57f)
        repeat(32) { index ->
            val angle = (index * 137.508 + 210) * PI / 180
            val distance = (60 + (index * 47 % 160)).dp.toPx() * t
            val point = origin + Offset(cos(angle).toFloat() * distance,
                sin(angle).toFloat() * distance - 100.dp.toPx() * t + 170.dp.toPx() * t * t)
            val alpha = ((1 - t) * 1.6f).coerceIn(0f, 1f)
            val color = (if (index % 3 == 0) Gold else PaperColors[index % 5]).copy(alpha = alpha)
            if (index % 4 == 0) {
                val r = (3 + index % 4).dp.toPx() * (1 - t * .3f)
                drawLine(color, point - Offset(r, 0f), point + Offset(r, 0f), 1.5.dp.toPx(), StrokeCap.Round)
                drawLine(color, point - Offset(0f, r), point + Offset(0f, r), 1.5.dp.toPx(), StrokeCap.Round)
            } else {
                withTransform({ rotate(t * (index % 5 + 1) * 90, point) }) {
                    drawRoundRect(color, point, Size(5.dp.toPx(), (3 + index % 5).dp.toPx()), androidx.compose.ui.geometry.CornerRadius(1.dp.toPx()))
                }
            }
        }
    }
}
