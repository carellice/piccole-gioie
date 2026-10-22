package it.piccolegioie.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.*

private data class WelcomePage(val eyebrow: String, val title: String, val description: String)
private val WelcomePages = listOf(
    WelcomePage("01 · FAI SPAZIO AL BELLO", "La felicità abita\nnelle cose piccole.",
        "Un barattolo tutto tuo per custodire i momenti felici. Anche quelli che sembrano piccolissimi."),
    WelcomePage("02 · UN MINUTO, SOLO TUO", "Un pensiero.\nUn piccolo tesoro.",
        "Scrivi una cosa bella o un grazie. Scegli il colore del tuo bigliettino e lascialo nel barattolo."),
    WelcomePage("03 · IL BELLO RESTA CON TE", "Un sorriso da ritrovare.\nQuando ne hai bisogno.",
        "Pesca un ricordo nei giorni no, oppure riapri il tuo anno. Ci sarà sempre un pezzetto di bello ad aspettarti.")
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pager = rememberPagerState(pageCount = { WelcomePages.size })
    val scope = rememberCoroutineScope()
    var finishing by remember { mutableStateOf(false) }
    val current = pager.currentPage
    val background by animateColorAsState(listOf(Cream, Color(0xFFF3F5EC), Color(0xFFFAF3E9))[current], tween(650), label = "welcome light")
    val finish: () -> Unit = { if (!finishing) { finishing = true; onFinished() } }
    BackHandler(current > 0 && !finishing) {
        scope.launch { pager.animateScrollToPage((current - 1).coerceAtLeast(0), animationSpec = tween(500, easing = FastOutSlowInEasing)) }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(background)) {
        val headlineSize = if (maxWidth < 380.dp) 28.sp else 32.sp
        val headlineLineHeight = if (maxWidth < 380.dp) 34.sp else 38.sp
        WelcomeAtmosphere(current, Modifier.matchParentSize())
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 26.dp).padding(top = 8.dp).height(50.dp).joyEntrance(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.WbSunny, null, Modifier.size(24.dp), Gold)
                Spacer(Modifier.width(9.dp))
                Text("piccole gioie", color = Forest, fontSize = 18.sp, letterSpacing = (-.6).sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                TextButton(finish, enabled = !finishing) { Text("Salta", color = Muted, fontSize = 12.sp) }
            }
            HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth(), userScrollEnabled = !finishing, key = { it }) { index ->
                val offset = (pager.currentPage - index + pager.currentPageOffsetFraction).coerceIn(-1f, 1f)
                val page = WelcomePages[index]
                Column(Modifier.fillMaxSize().padding(horizontal = 26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    OnboardingScene(index, Modifier.weight(1f).fillMaxWidth().graphicsLayer {
                        translationX = offset * size.width * .13f
                        rotationZ = offset * -4f
                        scaleX = 1f - abs(offset) * .1f; scaleY = scaleX
                        alpha = 1f - abs(offset) * .5f
                    })
                    Column(Modifier.fillMaxWidth().graphicsLayer {
                        translationX = offset * size.width * .25f
                        alpha = 1f - abs(offset) * .65f
                    }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Eyebrow(page.eyebrow, color = Gold, modifier = Modifier.joyEntrance(1))
                        Spacer(Modifier.height(13.dp))
                        Text(page.title, style = DisplayStyle.copy(fontSize = headlineSize, lineHeight = headlineLineHeight, letterSpacing = (-.9).sp),
                            textAlign = TextAlign.Center, modifier = Modifier.joyEntrance(2))
                        Spacer(Modifier.height(13.dp))
                        Text(page.description, color = Muted, fontSize = 14.sp, lineHeight = 23.sp,
                            textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 340.dp).joyEntrance(3))
                        Spacer(Modifier.height(21.dp))
                    }
                }
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(bottom = 8.dp).joyEntrance(3)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.height(8.dp).weight(1f).semantics {
                        contentDescription = "Pagina ${current + 1} di ${WelcomePages.size}"
                        progressBarRangeInfo = ProgressBarRangeInfo(current.toFloat(), 0f..2f, 1)
                    }, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(WelcomePages.size) { index ->
                            val width by animateDpAsState(if (index == current) 27.dp else 7.dp, spring(.65f, 350f), label = "welcome step")
                            val color by animateColorAsState(if (index == current) Forest else Forest.copy(alpha = .13f), tween(250), label = "step color")
                            Box(Modifier.width(width).height(7.dp).background(color, CircleShape))
                        }
                    }
                    AnimatedContent(current, transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) }, label = "welcome page number") {
                        Text("0${it + 1} / 03", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
                    }
                }
                PrimaryButton(if (current == 2) "Crea il mio barattolo" else if (current == 0) "Scopri come" else "Continua",
                    if (current == 2) Icons.Outlined.AutoAwesome else Icons.AutoMirrored.Outlined.ArrowForward,
                    enabled = !finishing && !pager.isScrollInProgress) {
                    if (current == 2) finish()
                    else scope.launch { pager.animateScrollToPage(current + 1, animationSpec = tween(550, easing = FastOutSlowInEasing)) }
                }
                Row(Modifier.fillMaxWidth().height(43.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    if (current > 0) TextButton({ scope.launch { pager.animateScrollToPage(current - 1, animationSpec = tween(500)) } }, enabled = !finishing && !pager.isScrollInProgress) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, Modifier.size(13.dp), Muted)
                        Spacer(Modifier.width(6.dp)); Text("Indietro", color = Muted, fontSize = 11.sp)
                    } else Text("Un giorno alla volta. Senza fretta.", color = Muted, fontSize = 11.sp)
                    if (current == 2) { Spacer(Modifier.width(9.dp)); Text("Uno spazio privato, tutto tuo.", color = Muted, fontSize = 10.sp) }
                }
            }
        }
    }
}

@Composable
private fun WelcomeAtmosphere(page: Int, modifier: Modifier) {
    val infinite = rememberInfiniteTransition(label = "welcome afternoon")
    val phase = infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(18000, easing = LinearEasing)), label = "slow golden dust")
    val warmth by animateColorAsState(if (page == 1) Color(0xFFCEDCC4) else Color(0xFFEBD7AC), tween(650), label = "ambient warmth")
    Canvas(modifier) {
        val t = phase.value
        val glowCenter = Offset(size.width * (.55f + sin(t * PI * 2).toFloat() * .06f), size.height * .32f)
        drawCircle(Brush.radialGradient(listOf(warmth.copy(alpha = .4f), Color.Transparent), glowCenter, size.width * .7f), size.width * .7f, glowCenter)
        repeat(15) { index ->
            val age = (t + index * .137f) % 1f
            val x = size.width * (.07f + (index * 37 % 85) / 100f) + sin(age * PI * 2 + index).toFloat() * 12.dp.toPx()
            val y = size.height * (.57f - age * .48f)
            val alpha = sin(age * PI).toFloat().coerceAtLeast(0f) * .35f
            val r = (1f + index % 3 * .7f).dp.toPx()
            if (index % 4 == 0) drawPath(Path().apply {
                moveTo(x, y - r * 2); quadraticTo(x, y, x + r * 2, y)
                quadraticTo(x, y, x, y + r * 2); quadraticTo(x, y, x - r * 2, y); quadraticTo(x, y, x, y - r * 2)
            }, Gold.copy(alpha = alpha))
            else drawCircle(Gold.copy(alpha = alpha), r, Offset(x, y))
        }
    }
}
