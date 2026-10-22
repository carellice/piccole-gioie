package it.piccolegioie.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

val Cream = Color(0xFFF9F6EF)
val Forest = Color(0xFF314F43)
val Ink = Color(0xFF303D34)
val Muted = Color(0xFF7D8176)
val Gold = Color(0xFFAF7D43)
val Line = Color(0xFFE7E4D9)
val PaperColors = listOf(Color(0xFFF2DDAC), Color(0xFFDFE7D7), Color(0xFFF1D8CE), Color(0xFFE1DCEE), Color(0xFFD6E5E7))
val Categories = listOf("Piccole gioie", "Gratitudine", "Persone", "Traguardi")
val Italian: Locale = Locale.ITALIAN
fun LocalDate.pretty(): String = format(DateTimeFormatter.ofPattern("d MMMM yyyy", Italian))
fun LocalDate.shortDate(): String = format(DateTimeFormatter.ofPattern("d MMM", Italian))
val DisplayStyle = TextStyle(fontFamily = FontFamily.Serif, color = Ink, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.9).sp)

/** Clearance at the end of a list, while its scrolling viewport continues behind the dock. */
@Composable
fun floatingContentBottomPadding() = if (WindowInsets.ime.getBottom(LocalDensity.current) > 0) 24.dp
    else 132.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

@Composable
fun JoyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = Forest, onPrimary = Color.White, secondary = Gold,
        background = Cream, onBackground = Ink, surface = Cream, onSurface = Ink,
        surfaceVariant = Color(0xFFEFEEE5), onSurfaceVariant = Muted,
        outline = Line, outlineVariant = Line, primaryContainer = Color(0xFFE5EBDF), onPrimaryContainer = Forest,
        secondaryContainer = Color(0xFFE5EBDF), onSecondaryContainer = Forest, surfaceTint = Forest
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
    IconButton(onClick, modifier.size(46.dp).background(Color.White.copy(alpha = .62f), CircleShape).border(1.dp, Line, CircleShape)) {
        Icon(icon, label, Modifier.size(20.dp), tint)
    }
}

@Composable
fun PaperCard(memory: Memory, onClick: () -> Unit, modifier: Modifier = Modifier, compact: Boolean = false) {
    val interaction = remember { MutableInteractionSource() }
    Column(modifier.joyPress(interaction).clip(RoundedCornerShape(20.dp)).background(PaperColors[memory.color])
        .clickable(interactionSource = interaction, indication = null, onClick = onClick).padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(memory.date.shortDate(), Ink.copy(alpha = .6f), Modifier.weight(1f))
            if (memory.favorite) Icon(Icons.Outlined.Favorite, "Ricordo del cuore", Modifier.size(15.dp), Gold)
            else Text("✧", color = Ink.copy(alpha = .5f), fontSize = 17.sp)
        }
        Spacer(Modifier.height(13.dp))
        Text(memory.text, style = DisplayStyle.copy(fontSize = if (compact) 18.sp else 21.sp, lineHeight = 28.sp, letterSpacing = (-.3).sp),
            maxLines = if (compact) 3 else 5, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        Spacer(Modifier.height(16.dp))
        Text(memory.category, color = Ink.copy(alpha = .6f), fontSize = 11.sp)
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
