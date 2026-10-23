package it.piccolegioie.app

import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class JoyPalette(val cream: Color, val forest: Color, val ink: Color, val muted: Color, val gold: Color, val line: Color, val surface: Color, val papers: List<Color>)
private val lightPalette = JoyPalette(Color(0xFFF9F6EF), Color(0xFF314F43), Color(0xFF303D34), Color(0xFF7D8176), Color(0xFFAF7D43), Color(0xFFE7E4D9), Color(0xFFE8ECDF), listOf(Color(0xFFF2DDAC), Color(0xFFDFE7D7), Color(0xFFF1D8CE), Color(0xFFE1DCEE), Color(0xFFD6E5E7)))
private val darkPalette = JoyPalette(Color(0xFF191F1B), Color(0xFFB7D4BC), Color(0xFFEAE8DC), Color(0xFFB0B6AA), Color(0xFFE1B878), Color(0xFF495247), Color(0xFF2D372E), listOf(Color(0xFF55472F), Color(0xFF354A38), Color(0xFF563D36), Color(0xFF484055), Color(0xFF304A50)))
private var activePalette by androidx.compose.runtime.mutableStateOf(lightPalette)
val Cream get() = activePalette.cream
val Forest get() = activePalette.forest
val Ink get() = activePalette.ink
val Muted get() = activePalette.muted
val Gold get() = activePalette.gold
val Line get() = activePalette.line
val SoftSurface get() = activePalette.surface
val PaperColors get() = activePalette.papers
val Categories = listOf("Piccole gioie", "Gratitudine", "Persone", "Traguardi")
val Italian: Locale = Locale.ITALIAN
fun LocalDate.pretty(): String = format(DateTimeFormatter.ofPattern("d MMMM yyyy", Italian))
fun LocalDate.shortDate(): String = format(DateTimeFormatter.ofPattern("d MMM", Italian))
val DisplayStyle get() = TextStyle(fontFamily = FontFamily.Serif, color = Ink, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.9).sp)

/** Clearance at the end of a list, while its scrolling viewport continues behind the dock. */
@Composable
fun floatingContentBottomPadding() = if (WindowInsets.ime.getBottom(LocalDensity.current) > 0) 24.dp
    else 132.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

@Composable
fun JoyTheme(content: @Composable () -> Unit) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val palette = if (dark) darkPalette else lightPalette
    androidx.compose.runtime.SideEffect { activePalette = palette }
    val activity = androidx.activity.compose.LocalActivity.current as? androidx.activity.ComponentActivity
    androidx.compose.runtime.SideEffect {
        activity?.enableEdgeToEdge(
            statusBarStyle = if (dark) androidx.activity.SystemBarStyle.dark(0xFF191F1B.toInt()) else androidx.activity.SystemBarStyle.light(0xFFF9F6EF.toInt(), 0xFFF9F6EF.toInt()),
            navigationBarStyle = if (dark) androidx.activity.SystemBarStyle.dark(0xFF191F1B.toInt()) else androidx.activity.SystemBarStyle.light(0xFFF9F6EF.toInt(), 0xFFF9F6EF.toInt()))
    }
    val base = if (dark) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = base.copy(
        primary = palette.forest, onPrimary = palette.cream, secondary = palette.gold,
        background = palette.cream, onBackground = palette.ink, surface = palette.cream, onSurface = palette.ink,
        surfaceVariant = palette.surface, onSurfaceVariant = palette.muted,
        outline = palette.line, outlineVariant = palette.line, primaryContainer = palette.surface, onPrimaryContainer = palette.forest,
        secondaryContainer = palette.surface, onSecondaryContainer = palette.forest, surfaceTint = palette.forest
    ), typography = Typography(bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 25.sp),
        bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
        labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)), content = content)
}

@Composable
fun Eyebrow(text: String, color: Color = Muted, modifier: Modifier = Modifier) {
    Text(text.uppercase(Italian), modifier, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
}

@Composable
fun PrimaryButton(text: String, icon: ImageVector? = null, enabled: Boolean = true, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Button(onClick, Modifier.fillMaxWidth().height(58.dp).joyPress(interaction), enabled = enabled, interactionSource = interaction,
        shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(horizontal = 20.dp)) {
        if (icon != null) { Icon(icon, null, Modifier.size(19.dp)); Spacer(Modifier.width(10.dp)) }
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RoundIcon(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = Forest) {
    IconButton(onClick, modifier.size(46.dp).background(SoftSurface.copy(alpha = .62f), CircleShape).border(1.dp, Line, CircleShape)) {
        Icon(icon, label, Modifier.size(20.dp), tint)
    }
}

@Composable
fun PaperCard(memory: Memory, onClick: () -> Unit, modifier: Modifier = Modifier, compact: Boolean = false) {
    val interaction = remember { MutableInteractionSource() }
    Column(modifier.joyPress(interaction).clip(RoundedCornerShape(20.dp)).background(PaperColors[memory.color])
        .clickable(interactionSource = interaction, indication = null, onClick = onClick).padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(memory.date.year.toString(), Ink.copy(alpha = .6f), Modifier.weight(1f))
            if (memory.favorite) Icon(Icons.Outlined.Favorite, "Ricordo del cuore", Modifier.size(15.dp), Gold)
            else Text("✧", color = Ink.copy(alpha = .5f), fontSize = 17.sp)
        }
        Spacer(Modifier.height(13.dp))
        Text(memory.text, style = DisplayStyle.copy(fontSize = if (compact) 18.sp else 21.sp, lineHeight = 28.sp, letterSpacing = (-.3).sp),
            maxLines = if (compact) 3 else 5, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        Spacer(Modifier.height(16.dp))

    }
}

@Composable
fun GentleEmpty(title: String, body: String, icon: ImageVector, action: String? = null, onAction: () -> Unit = {}) {
    Column(Modifier.fillMaxWidth().joyEntrance(1).padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(80.dp).background(Color(0xFFEBEDE1), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(29.dp), Forest)
        }
        Spacer(Modifier.height(22.dp))
        Text(title, style = DisplayStyle.copy(fontSize = 26.sp, lineHeight = 33.sp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(body, color = Muted, fontSize = 14.sp, lineHeight = 23.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(horizontal = 15.dp))
        if (action != null) { Spacer(Modifier.height(22.dp)); PrimaryButton(action, onClick = onAction) }
    }
}
