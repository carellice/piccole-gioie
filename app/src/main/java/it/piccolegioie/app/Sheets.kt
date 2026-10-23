package it.piccolegioie.app

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteSheet(onDismiss: () -> Unit, onSave: (Memory, () -> Unit) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    var color by rememberSaveable { mutableIntStateOf(0) }
    var prompt by rememberSaveable { mutableIntStateOf(0) }
    var saving by rememberSaveable { mutableStateOf(false) }
    var discard by remember { mutableStateOf(false) }
    val prompts = listOf("Quale piccola cosa ti ha fatto sorridere oggi?", "Per cosa ti senti grato, proprio adesso?", "Chi ha reso più bella la tua giornata?", "Qual è un momento che vorresti tenere vicino?")
    val date = LocalDate.now()
    val pager = androidx.compose.foundation.pager.rememberPagerState(initialPage = color, pageCount = { PaperColors.size })
    LaunchedEffect(pager.settledPage) { color = pager.settledPage }
    val paperColor by animateColorAsState(PaperColors[color], tween(400), label = "paper tint")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dismissAnimated = rememberAnimatedSheetDismiss(sheetState, onDismiss)
    val requestClose = { if (text.isNotBlank() && !saving) discard = true else if (!saving) dismissAnimated() }
    ModalBottomSheet(onDismissRequest = requestClose, sheetState = sheetState,
        containerColor = Cream, dragHandle = { SheetHandle() }) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(start = 26.dp, end = 26.dp, bottom = 28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("UN PICCOLO MOMENTO", modifier = Modifier.weight(1f))
                IconButton(requestClose) { Icon(Icons.Outlined.Close, "Chiudi", tint = Muted) }
            }
            Text("Cosa vuoi custodire?", style = DisplayStyle.copy(fontSize = 31.sp))
            Spacer(Modifier.height(8.dp))
            Text("Non deve essere speciale per gli altri. Solo per te.", fontSize = 13.sp, lineHeight = 21.sp, color = Muted)
            Spacer(Modifier.height(22.dp))
            androidx.compose.foundation.pager.HorizontalPager(state = pager, pageSpacing = 12.dp,
                modifier = Modifier.fillMaxWidth(), userScrollEnabled = !saving) { page ->
                Column(Modifier.fillMaxWidth().joyUnfold(Unit).clip(RoundedCornerShape(8.dp, 8.dp, 28.dp, 8.dp))
                    .background(PaperColors[page]).padding(22.dp)) {
                    Text(date.year.toString(), color = Ink.copy(alpha = .6f), fontSize = 11.sp)
                    Spacer(Modifier.height(18.dp))
                    BasicTextField(text, { if (it.length <= 2000) text = it }, Modifier.fillMaxWidth().heightIn(min = 160.dp), enabled = !saving,
                        textStyle = DisplayStyle.copy(fontSize = 23.sp, lineHeight = 33.sp), cursorBrush = SolidColor(Forest),
                        decorationBox = { inner -> Box { if (text.isEmpty()) Text("Oggi è stato bello quando…", style = DisplayStyle.copy(fontSize = 23.sp, lineHeight = 33.sp, color = Ink.copy(alpha = .5f))); inner() } })
                    Text("${text.length} / 2000", Modifier.fillMaxWidth(), textAlign = TextAlign.End, fontSize = 10.sp, color = Ink.copy(alpha = .6f))
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("${listOf("Miele", "Salvia", "Pesca", "Lavanda", "Cielo")[pager.currentPage]} · Scorri per cambiare colore", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Muted, fontSize = 11.sp)
            Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.Center) {
                PaperColors.forEachIndexed { index, paper ->
                    Box(Modifier.padding(horizontal = 4.dp).size(if (pager.currentPage == index) 9.dp else 6.dp).clip(CircleShape).background(if (pager.currentPage == index) Forest else Muted.copy(alpha = .45f)))
                }
            }
            PrimaryButton(if (saving) "Sto custodendo il tuo momento…" else "Metti nel barattolo", Icons.Outlined.Add, text.isNotBlank() && !saving) {
                saving = true
                onSave(Memory(text = text.trim(), date = date, color = pager.settledPage)) { saving = false }
            }
            Spacer(Modifier.height(10.dp))
            Text("Solo tuo. Al sicuro nel tuo barattolo.", Modifier.fillMaxWidth(), color = Muted, fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("Lasciamo il bigliettino?") },
        text = { Text("Il momento non è ancora nel barattolo. Puoi continuare a scriverlo o chiuderlo senza salvarlo.") },
        confirmButton = { TextButton({ discard = false; dismissAnimated() }) { Text("Chiudi senza salvare") } },
        dismissButton = { TextButton({ discard = false }) { Text("Continuo a scrivere") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemorySheet(memory: Memory, random: Boolean, onDismiss: () -> Unit, onFavorite: () -> Unit, onDelete: () -> Unit, onAgain: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dismissAnimated = rememberAnimatedSheetDismiss(sheetState, onDismiss)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Cream, dragHandle = { SheetHandle() }) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(start = 26.dp, end = 26.dp, bottom = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(if (random) "UN SORRISO DAL TUO BARATTOLO" else "IL TUO PICCOLO MOMENTO", modifier = Modifier.weight(1f))
                IconButton(dismissAnimated) { Icon(Icons.Outlined.Close, "Chiudi", tint = Muted) }
            }
            if (random) {
                Text("Il bello è ancora qui.", style = DisplayStyle.copy(fontSize = 31.sp))
                Spacer(Modifier.height(10.dp))
                Text("È successo davvero. E fa parte di te.", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(25.dp))
            }
            Column(Modifier.fillMaxWidth().joyUnfold(memory.id).clip(RoundedCornerShape(8.dp, 8.dp, 30.dp, 8.dp)).background(PaperColors[memory.color]).padding(26.dp)) {
                Eyebrow(memory.date.year.toString(), Ink.copy(alpha = .6f))
                Spacer(Modifier.height(20.dp))
                Text(memory.text, style = DisplayStyle.copy(fontSize = 26.sp, lineHeight = 37.sp, letterSpacing = (-.4).sp))
                Spacer(Modifier.height(25.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.weight(1f))
                    Text("✧", fontSize = 25.sp, color = Gold)
                }
            }
            Spacer(Modifier.height(17.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                JoyFavorite(memory.favorite, onFavorite)
                Spacer(Modifier.width(18.dp))
                IconButton({ confirmDelete = true }) { Icon(Icons.Outlined.DeleteOutline, "Elimina ricordo", Modifier.size(20.dp), Muted) }
            }
            if (random) { Spacer(Modifier.height(15.dp)); PrimaryButton("Pesca un altro sorriso", Icons.Outlined.Shuffle, onClick = onAgain) }
        }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("Lasciar andare questo ricordo?") },
        text = { Text("Il bigliettino verrà eliminato dal barattolo. Questa azione non può essere annullata.") },
        confirmButton = { TextButton({ confirmDelete = false; onDelete() }) { Text("Elimina", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton({ confirmDelete = false }) { Text("Lo tengo") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearSheet(year: Int, memories: List<Memory>, onDismiss: () -> Unit, onOpen: (Memory) -> Unit) {
    val notes = memories.filter { it.date.year == year }
    val favorite = notes.filter { it.favorite }.ifEmpty { notes }.take(3)
    val days = notes.map { it.date }.distinct().size
    val current = year == LocalDate.now().year
    val progress = remember { Animatable(0f) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dismissAnimated = rememberAnimatedSheetDismiss(sheetState, onDismiss)
    LaunchedEffect(year) { progress.snapTo(0f); progress.animateTo(MemoryStats.yearProgress(), tween(1200, delayMillis = 200)) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Cream, dragHandle = { SheetHandle() }) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(start = 26.dp, end = 26.dp, bottom = 32.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("IL TUO ANNO IN UN BARATTOLO", modifier = Modifier.weight(1f))
                IconButton(dismissAnimated) { Icon(Icons.Outlined.Close, "Chiudi", tint = Muted) }
            }
            Text("$year", style = DisplayStyle.copy(fontSize = 70.sp, lineHeight = 77.sp, color = Forest))
            Text(if (current) "Quanta bellezza, fin qui." else "C’era tanto bello, in quei giorni.", style = DisplayStyle.copy(fontSize = 29.sp, lineHeight = 36.sp))
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("${notes.size}" to "momenti custoditi").forEach { (n, label) ->
                    Column(Modifier.weight(1f).joyEntrance(1).clip(RoundedCornerShape(20.dp)).background(SoftSurface).padding(20.dp)) {
                        AnimatedCount(n.toInt(), style = DisplayStyle.copy(fontSize = 34.sp, color = Forest)); Text(label, color = Muted, fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            if (current) {
                LinearProgressIndicator(progress = { progress.value }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape), color = Forest, trackColor = Line)
                Spacer(Modifier.height(10.dp))
                Text("Il tuo anno sta ancora fiorendo. C’è spazio per altri sorrisi.", fontSize = 12.sp, lineHeight = 20.sp, color = Muted)
                Spacer(Modifier.height(25.dp))
            }
            if (notes.isNotEmpty()) {
                Eyebrow("MOMENTI DA PORTARE CON TE")
                Spacer(Modifier.height(13.dp))
                favorite.forEach { note -> PaperCard(note, { onOpen(note) }, Modifier.fillMaxWidth(), true); Spacer(Modifier.height(12.dp)) }
                val most = notes.groupingBy { it.category }.eachCount().maxByOrNull { it.value }
                if (most != null) Text("Quest’anno hai raccolto soprattutto: ${most.key.lowercase(Italian)}.", color = Muted, fontSize = 12.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 10.dp))
            } else {
                GentleEmpty("Un anno da riempire di bello.", "Ogni ricordo che scriverai diventerà parte di questo piccolo racconto.", Icons.Outlined.LocalFlorist)
            }
        }
    }
}

/** Keep the sheet composed until Material's exit animation has actually finished. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberAnimatedSheetDismiss(sheetState: SheetState, onDismiss: () -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    var closing by remember { mutableStateOf(false) }
    return remember(sheetState, scope) {
        {
            if (!closing) {
                closing = true
                scope.launch {
                    try {
                        sheetState.hide()
                        if (!sheetState.isVisible) currentOnDismiss()
                    } finally {
                        closing = false
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetHandle() { Box(Modifier.padding(top = 12.dp, bottom = 8.dp).size(34.dp, 4.dp).clip(CircleShape).background(Line)) }
