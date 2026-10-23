package it.piccolegioie.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun CozyDock(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val position = animateFloatAsState(selected.toFloat(), spring(dampingRatio = .67f, stiffness = 330f), label = "gliding pebble")
    val ripple = remember { Animatable(1f) }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(selected) { ripple.snapTo(0f); ripple.animateTo(1f, tween(750)) }
    Box(modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 26.dp).padding(top = 12.dp, bottom = 18.dp).joyEntrance(3)) {
        BoxWithConstraints(Modifier.fillMaxWidth().shadow(18.dp, RoundedCornerShape(32.dp), ambientColor = Forest.copy(alpha = .15f), spotColor = Forest.copy(alpha = .15f))
            .background(Brush.verticalGradient(listOf(Cream, SoftSurface)), RoundedCornerShape(32.dp))
            .border(1.dp, Line, RoundedCornerShape(32.dp)).height(86.dp).padding(horizontal = 10.dp).selectableGroup()) {
            val slot = maxWidth / 3
            // One continuous moving object connects the three destinations.
            Box(Modifier.offset(x = slot * position.value + (slot - 50.dp) / 2, y = 7.dp)
                .size(50.dp).graphicsLayer {
                    val pulse = sin(ripple.value * PI).toFloat()
                    scaleX = 1 + .1f * pulse; scaleY = 1 - .06f * pulse
                }.shadow(8.dp, CircleShape, ambientColor = Forest, spotColor = Forest.copy(alpha = .3f))
                .background(Brush.linearGradient(listOf(Color(0xFF426552), Color(0xFF243F35))), CircleShape)
                .border(1.dp, Color.White.copy(alpha = .15f), CircleShape))
            Canvas(Modifier.fillMaxSize()) {
                val x = size.width / 3 * (position.value + .5f)
                val t = ripple.value
                if (t > 0 && t < 1) repeat(6) { i ->
                    val angle = i * PI / 3
                    val r = (24 + 17 * t).dp.toPx()
                    drawCircle(Gold.copy(alpha = (1 - t) * .65f), (1.6f * (1 - t)).dp.toPx(), Offset(x + cos(angle).toFloat() * r, 32.dp.toPx() + sin(angle).toFloat() * r))
                }
                drawCircle(Gold, 2.dp.toPx(), Offset(x, 77.dp.toPx()))
            }
            Row(Modifier.fillMaxSize()) {
                listOf("Barattolo", "Ricordi", "Rituale").forEachIndexed { index, title ->
                    val interaction = remember { MutableInteractionSource() }
                    val active = selected == index
                    val color by animateColorAsState(if (active) Forest else Muted, tween(220), label = "dock label")
                    Column(Modifier.weight(1f).fillMaxHeight().joyPress(interaction)
                        .selectable(active, interactionSource = interaction, indication = null, role = Role.Tab) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelect(index)
                        }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(16.dp))
                        DockObject(index, active, Modifier.size(31.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(title, color = color, fontSize = 10.sp, lineHeight = 15.sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

/** Tiny objects drawn specifically for the dock: a cork jar, paper pages and a sunrise. */
@Composable
private fun DockObject(index: Int, selected: Boolean, modifier: Modifier) {
    val selection = animateFloatAsState(if (selected) 1f else 0f, spring(.6f, 240f), label = "object lift")
    val flourish = remember { Animatable(1f) }
    LaunchedEffect(selected) { if (selected) { flourish.snapTo(0f); flourish.animateTo(1f, tween(850)) } }
    val color by animateColorAsState(if (selected) Color(0xFFFFF8DC) else Muted, tween(180), label = "object ink")
    Canvas(modifier.graphicsLayer {
        val bounce = sin(flourish.value * PI).toFloat()
        translationY = -bounce * 4.dp.toPx()
        rotationZ = if (index == 0) sin(flourish.value * PI * 3).toFloat() * 9 else 0f
        scaleX = 1 + selection.value * .04f; scaleY = scaleX
    }) {
        scale(size.width / 32, size.height / 32, Offset.Zero) {
            val line = Stroke(1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            val motion = sin(flourish.value * PI).toFloat()
            when (index) {
                0 -> {
                    drawRoundRect(color.copy(alpha = .1f), Offset(7f, 10f), Size(18f, 18f), CornerRadius(5f))
                    drawPath(Path().apply { moveTo(10f, 9f); lineTo(22f, 9f); lineTo(22f, 12f); quadraticTo(26f, 13f, 26f, 17f); lineTo(26f, 25f); quadraticTo(26f, 29f, 22f, 29f); lineTo(10f, 29f); quadraticTo(6f, 29f, 6f, 25f); lineTo(6f, 17f); quadraticTo(6f, 13f, 10f, 12f); close() }, color, style = line)
                    translate(0f, -3f * motion) { rotate(-8 * motion, Offset(10f, 6f)) {
                        drawRoundRect(if (selected) Color(0xFFE5C58B) else color, Offset(9f, 4f), Size(14f, 4f), CornerRadius(1.2f))
                    } }
                    rotate(-16f, Offset(16f, 22f)) { drawRoundRect(color.copy(alpha = .75f), Offset(12f, 19f), Size(9f, 5f), CornerRadius(.5f)) }
                    drawLine(color.copy(alpha = .6f), Offset(9f, 17f), Offset(9f, 23f), 1f, StrokeCap.Round)
                }
                1 -> {
                    drawPath(Path().apply { moveTo(16f, 9f); quadraticTo(10f, 5f, 3f, 7f); lineTo(3f, 25f); quadraticTo(11f, 23f, 16f, 27f); quadraticTo(22f, 23f, 29f, 25f); lineTo(29f, 7f); quadraticTo(22f, 5f, 16f, 9f); lineTo(16f, 27f) }, color, style = line)
                    for (y in listOf(12f, 16f, 20f)) {
                        drawPath(Path().apply { moveTo(6f, y); quadraticTo(10f, y - 1f, 13f, y + 1f) }, color.copy(alpha = .6f), style = Stroke(1f, cap = StrokeCap.Round))
                        drawPath(Path().apply { moveTo(19f, y + 1f); quadraticTo(23f, y - 1f - motion * 2, 26f, y) }, color.copy(alpha = .6f), style = Stroke(1f, cap = StrokeCap.Round))
                    }
                    if (selected) drawPath(Path().apply { moveTo(16f, 26f); quadraticTo(11f - motion * 6, 16f, 16f - motion * 8, 7f) }, Color(0xFFE5C58B).copy(alpha = motion), style = line)
                }
                else -> {
                    drawArc(color, 180f, 180f, false, Offset(9f, 14f - motion * 2), Size(14f, 14f), style = line)
                    drawLine(color, Offset(4f, 22f), Offset(28f, 22f), 1.5f, StrokeCap.Round)
                    drawLine(color.copy(alpha = .5f), Offset(10f, 26f), Offset(22f, 26f), 1.5f, StrokeCap.Round)
                    rotate(motion * 14f, Offset(16f, 20f)) {
                        repeat(5) { ray ->
                            val a = (195 + ray * 37) * PI / 180
                            drawLine(if (selected) Color(0xFFE5C58B) else color,
                                Offset(16f + cos(a).toFloat() * 11f, 20f + sin(a).toFloat() * 11f),
                                Offset(16f + cos(a).toFloat() * 14f, 20f + sin(a).toFloat() * 14f), 1.5f, StrokeCap.Round)
                        }
                    }
                }
            }
        }
    }
}
