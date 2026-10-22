package it.piccolegioie.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun OnboardingScene(page: Int, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
        val sceneScale = min(maxWidth / 360.dp, maxHeight / 350.dp).coerceAtMost(1f)
        Box(Modifier.align(Alignment.Center).requiredSize(360.dp, 350.dp).graphicsLayer {
            scaleX = sceneScale; scaleY = sceneScale
        }) {
            when (page) {
                0 -> WelcomeJarScene()
                1 -> WelcomePaperScene()
                else -> WelcomeSmileScene()
            }
        }
    }
}

@Composable
private fun BoxScope.WelcomeJarScene() {
    val infinite = rememberInfiniteTransition(label = "first little joy")
    val float = infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(5200, easing = LinearEasing)), label = "orbiting keepsakes")
    var shake by remember { mutableIntStateOf(0) }
    Canvas(Modifier.matchParentSize()) {
        rotate(float.value * 360f, center) {
            drawOval(Gold.copy(alpha = .16f), Offset(26.dp.toPx(), 26.dp.toPx()), Size(308.dp.toPx(), 292.dp.toPx()),
                style = Stroke(.8.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 8.dp.toPx()))))
        }
    }
    HappinessJar(5, Modifier.align(Alignment.Center).width(340.dp).clearAndSetSemantics {
        contentDescription = "Illustrazione del tuo barattolo della felicità"
        onClick(label = "Anima il barattolo") { shake++; true }
    }, fishSignal = shake, onTap = { shake++ })
    Box(Modifier.align(Alignment.TopStart).offset(14.dp, 40.dp).graphicsLayer {
        rotationZ = -11f + sin(float.value * PI * 2).toFloat() * 3f
        translationY = sin(float.value * PI * 2).toFloat() * 7.dp.toPx()
    }.shadow(7.dp, RoundedCornerShape(14.dp)).background(Color(0xFFFFFCF2), RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text("un sorriso", style = DisplayStyle.copy(fontSize = 17.sp, lineHeight = 22.sp, fontStyle = FontStyle.Italic), color = Forest)
    }
    Box(Modifier.align(Alignment.CenterEnd).offset((-7).dp, 38.dp).size(67.dp, 58.dp).graphicsLayer {
        rotationZ = 15f + sin(float.value * PI * 2).toFloat() * 4f
        translationY = -sin(float.value * PI * 2).toFloat() * 6.dp.toPx()
    }.shadow(5.dp, RoundedCornerShape(6.dp)).background(PaperColors[2], RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.FavoriteBorder, null, Modifier.size(27.dp), Gold)
    }
    Text("Tocca il barattolo e guarda cosa succede", modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp), color = Gold, fontSize = 10.sp)
}

@Composable
private fun BoxScope.WelcomePaperScene() {
    val infinite = rememberInfiniteTransition(label = "paper on the desk")
    val phase = infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(5800, easing = LinearEasing)), label = "paper breathing")
    var sample by remember { mutableIntStateOf(0) }
    val paper by animateColorAsState(PaperColors[sample % PaperColors.size], tween(450), label = "a color for a feeling")
    val words = listOf("Il caffè al sole,\nbevuto senza fretta.", "La telefonata\ninaspettata\ndi un amico.", "Quel piccolo passo\nche ho fatto\nper me.", "Una risata\nche mi è rimasta\naddosso.", "Il profumo buono\ndella pioggia.")
    Box(Modifier.align(Alignment.Center).offset((-42).dp, 4.dp).size(230.dp, 224.dp).graphicsLayer { rotationZ = -13f }
        .shadow(9.dp, RoundedCornerShape(8.dp)).background(PaperColors[2], RoundedCornerShape(8.dp)))
    Box(Modifier.align(Alignment.Center).offset(34.dp, (-9).dp).size(232.dp, 225.dp).graphicsLayer { rotationZ = 10f }
        .shadow(9.dp, RoundedCornerShape(8.dp)).background(PaperColors[1], RoundedCornerShape(8.dp)))
    ExampleNote(words[sample % words.size], paper, Modifier.align(Alignment.Center).size(252.dp, 242.dp).joyUnfold(sample).graphicsLayer {
        rotationZ = -3f + sin(phase.value * PI * 2).toFloat() * 1.7f
        translationY = sin(phase.value * PI * 2).toFloat() * 5.dp.toPx()
    }, footnote = "UN’IDEA DA CUSTODIRE", onTap = { sample++ })
    Canvas(Modifier.align(Alignment.TopEnd).offset((-3).dp, 26.dp).size(148.dp, 24.dp).graphicsLayer {
        rotationZ = -43f + sin(phase.value * PI * 2).toFloat() * 3f
        translationY = sin(phase.value * PI * 2).toFloat() * 4.dp.toPx()
    }) {
        scale(size.width / 148f, size.height / 24f, Offset.Zero) {
            drawRoundRect(Ink.copy(alpha = .1f), Offset(18f, 7f), Size(121f, 16f), CornerRadius(3f))
            drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFECC786), Color(0xFFBE8D49), Color(0xFFEAC17C))), Offset(21f, 2f), Size(111f, 16f), CornerRadius(2f))
            drawLine(Color(0xFFFFE3AD), Offset(23f, 6f), Offset(126f, 6f), 1.5f)
            drawPath(Path().apply { moveTo(21f, 2f); lineTo(2f, 10f); lineTo(21f, 18f); close() }, Color(0xFFE0C6A1))
            drawPath(Path().apply { moveTo(8f, 7.5f); lineTo(2f, 10f); lineTo(8f, 12.5f); close() }, Forest)
            drawRect(Color(0xFFC1C4AC), Offset(125f, 2f), Size(10f, 16f))
            drawRoundRect(Color(0xFFDBB7A9), Offset(135f, 2f), Size(11f, 16f), CornerRadius(3f))
        }
    }
    Box(Modifier.align(Alignment.BottomEnd).offset((-8).dp, (-15).dp).size(72.dp).graphicsLayer {
        rotationZ = 10f; translationY = -sin(phase.value * PI * 2).toFloat() * 4.dp.toPx()
    }.shadow(4.dp, CircleShape).background(Color(0xFFFFFCF3), CircleShape), contentAlignment = Alignment.Center) {
        Text("un minuto\nper te", color = Gold, fontSize = 12.sp, lineHeight = 17.sp, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center)
    }
    Text("Tocca il bigliettino: ogni colore, una gioia", Modifier.align(Alignment.BottomCenter).padding(bottom = 1.dp), color = Gold, fontSize = 10.sp)
}

@Composable
private fun BoxScope.WelcomeSmileScene() {
    var sample by remember { mutableIntStateOf(0) }
    var shake by remember { mutableIntStateOf(0) }
    var fishing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val rise = remember { Animatable(0f) }
    val infinite = rememberInfiniteTransition(label = "a memory returns")
    val phase = infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(6500, easing = LinearEasing)), label = "floating memory")
    LaunchedEffect(sample) { rise.snapTo(0f); rise.animateTo(1f, spring(.72f, 95f)) }
    val words = listOf("Quel giorno\nho riso davvero.", "Qualcuno\nha pensato a me.", "È stato bello\nessere qui.")
    val fish: () -> Unit = {
        if (!fishing) {
            fishing = true; shake++
            scope.launch { delay(580); sample++; fishing = false }
        }
    }
    HappinessJar(8, Modifier.align(Alignment.BottomCenter).offset(y = 12.dp).width(314.dp).height(240.dp)
        .clearAndSetSemantics { contentDescription = "Un barattolo illustrato pieno di piccoli momenti felici" }, fishSignal = shake, onTap = fish)
    ExampleNote(words[sample % words.size], PaperColors[(sample + 1) % PaperColors.size],
        Modifier.align(Alignment.TopCenter).offset(y = 11.dp).size(222.dp, 156.dp).graphicsLayer {
            val t = rise.value
            translationY = (1 - t) * 110.dp.toPx() + sin(phase.value * PI * 2).toFloat() * 5.dp.toPx()
            rotationZ = (1 - t) * 15f - 5f + sin(phase.value * PI * 2).toFloat() * 2f
            alpha = (t * 3).coerceIn(0f, 1f)
            scaleX = .8f + .2f * t; scaleY = scaleX
        }, footnote = "IL BELLO È ANCORA QUI", onTap = fish)
    TextButton(fish, Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp), enabled = !fishing,
        shape = CircleShape, colors = ButtonDefaults.textButtonColors(containerColor = Forest, contentColor = Cream)) {
        Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(15.dp))
        Spacer(Modifier.width(8.dp)); Text("Pesca un sorriso", fontSize = 11.sp)
    }
}

@Composable
private fun ExampleNote(text: String, color: Color, modifier: Modifier, footnote: String, onTap: () -> Unit) {
    val shape = RoundedCornerShape(7.dp, 7.dp, 26.dp, 7.dp)
    val interaction = remember { MutableInteractionSource() }
    Box(modifier.joyPress(interaction).shadow(14.dp, shape, ambientColor = Forest.copy(alpha = .16f), spotColor = Forest.copy(alpha = .18f))
        .clip(shape).background(color).clickable(interactionSource = interaction, indication = null, onClick = onTap)) {
        Canvas(Modifier.matchParentSize()) {
            repeat(100) { i ->
                drawCircle(Ink.copy(alpha = .035f), .4.dp.toPx(), Offset((i * 137 % 997) / 997f * size.width, (i * 197 % 991) / 991f * size.height))
            }
            val fold = 21.dp.toPx()
            drawPath(Path().apply { moveTo(size.width - fold, size.height); lineTo(size.width - fold, size.height - fold); lineTo(size.width, size.height - fold); close() }, Ink.copy(alpha = .08f))
            drawLine(Color.White.copy(alpha = .5f), Offset(3.dp.toPx(), 1.dp.toPx()), Offset(size.width - 3.dp.toPx(), 1.dp.toPx()), 1.dp.toPx())
        }
        Column(Modifier.fillMaxSize().padding(21.dp), verticalArrangement = Arrangement.Center) {
            Text("✧", color = Gold, fontSize = 20.sp, lineHeight = 22.sp)
            Spacer(Modifier.height(9.dp))
            Text(text, style = DisplayStyle.copy(fontSize = 23.sp, lineHeight = 29.sp, letterSpacing = (-.4).sp), modifier = Modifier.weight(1f))
            Spacer(Modifier.height(9.dp))
            Text(footnote, color = Ink.copy(alpha = .5f), fontSize = 8.sp, lineHeight = 13.sp, letterSpacing = 1.1.sp)
        }
    }
}
