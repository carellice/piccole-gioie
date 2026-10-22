package it.piccolegioie.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(memories: List<Memory>, ready: Boolean, onAdd: () -> Unit, onFish: () -> Unit, onOpen: (Memory) -> Unit, onArchive: () -> Unit, onYear: () -> Unit) {
    val today = LocalDate.now()
    val thisYear = memories.count { it.date.year == today.year }
    val todayCount = memories.count { it.date == today }
    val bottomPadding = floatingContentBottomPadding()
    var fishSignal by remember { mutableIntStateOf(0) }
    var fishing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val requestFish: () -> Unit = {
        if (!fishing) {
            fishing = true; fishSignal++
            scope.launch { delay(650); onFish(); fishing = false }
        }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 26.dp, end = 26.dp, top = 18.dp, bottom = bottomPadding)) {
        item {
            Row(Modifier.fillMaxWidth().joyEntrance(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.WbSunny, null, Modifier.size(25.dp), Gold)
                Spacer(Modifier.width(10.dp))
                Text("piccole gioie", fontSize = 19.sp, color = Forest, fontWeight = FontWeight.Medium, letterSpacing = (-.7).sp)
                Spacer(Modifier.weight(1f))
                Row(Modifier.clip(CircleShape).border(1.dp, Line, CircleShape).clickable(onClick = onYear).padding(horizontal = 13.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(today.year.toString(), color = Forest, fontSize = 12.sp)
                    Spacer(Modifier.width(6.dp)); Icon(Icons.Outlined.ExpandMore, null, Modifier.size(14.dp), Forest)
                }
            }
            Spacer(Modifier.height(30.dp))
            Eyebrow(today.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Italian)), modifier = Modifier.fillMaxWidth().joyEntrance(1))
            Spacer(Modifier.height(10.dp))
            Text("La felicità,\nun pezzetto alla volta.", modifier = Modifier.joyEntrance(1), style = DisplayStyle.copy(fontSize = 35.sp, lineHeight = 40.sp))
            Spacer(Modifier.height(9.dp))
            Text("Le cose piccole fanno i giorni grandi.", color = Muted, fontSize = 14.sp)
            HappinessJar(thisYear, Modifier.padding(top = 4.dp), fishSignal = fishSignal, onTap = requestFish)
            Row(Modifier.fillMaxWidth().joyEntrance(3), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(5.dp).background(Gold, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(if (thisYear == 1) "1 piccolo momento, tutto tuo" else "$thisYear piccoli momenti, tutti tuoi", color = Muted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(if (todayCount > 0) "Custodisci un altro momento" else "Aggiungi un momento", Icons.Outlined.Add, ready, onAdd)
            TextButton(requestFish, Modifier.fillMaxWidth().height(48.dp), enabled = !fishing) {
                Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(17.dp), Gold)
                Spacer(Modifier.width(9.dp)); Text("Ho bisogno di un sorriso", color = Forest, fontSize = 13.sp)
            }
            Spacer(Modifier.height(23.dp))
            HorizontalDivider(color = Line)
            Spacer(Modifier.height(23.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("L’ultimo pezzetto di felicità", modifier = Modifier.weight(1f))
                IconButton(onArchive, Modifier.size(30.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Tutti i ricordi", Modifier.size(18.dp), Forest) }
            }
            Spacer(Modifier.height(12.dp))
            memories.firstOrNull()?.let { PaperCard(it, { onOpen(it) }, Modifier.fillMaxWidth(), compact = true) }
                ?: Column(Modifier.fillMaxWidth().border(1.dp, Line, RoundedCornerShape(20.dp)).padding(21.dp)) {
                    Text("Da dove cominciamo?", style = DisplayStyle.copy(fontSize = 22.sp, lineHeight = 27.sp))
                    Spacer(Modifier.height(8.dp))
                    Text("Un caffè al sole. Una telefonata. Qualcuno che ti ha fatto ridere. Basta un piccolo momento.", color = Muted, fontSize = 13.sp, lineHeight = 21.sp)
                }
            Spacer(Modifier.height(24.dp))
            Text("Un giorno alla volta. Senza fretta.", Modifier.fillMaxWidth(), color = Muted.copy(alpha = .8f), fontSize = 12.sp, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ArchiveScreen(memories: List<Memory>, onOpen: (Memory) -> Unit, onAdd: () -> Unit, onYear: (Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var favorites by rememberSaveable { mutableStateOf(false) }
    var year by rememberSaveable { mutableIntStateOf(LocalDate.now().year) }
    var showYears by remember { mutableStateOf(false) }
    val years = (memories.map { it.date.year } + LocalDate.now().year).distinct().sortedDescending()
    val bottomPadding = floatingContentBottomPadding()
    val filtered = memories.filter { it.date.year == year && (!favorites || it.favorite) && (it.text.contains(query, true) || it.category.contains(query, true)) }
        .sortedWith(compareByDescending<Memory> { it.date }.thenByDescending { it.createdAt })
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(26.dp, 24.dp, 26.dp, bottomPadding), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Eyebrow("LA TUA COLLEZIONE")
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Cose da ricordare.", style = DisplayStyle.copy(fontSize = 32.sp), modifier = Modifier.weight(1f))
                Box {
                    TextButton({ showYears = true }) { Text("$year"); Icon(Icons.Outlined.ExpandMore, null, Modifier.size(16.dp)) }
                    DropdownMenu(showYears, { showYears = false }) { years.forEach { y -> DropdownMenuItem({ Text("$y") }, { year = y; showYears = false }) } }
                }
            }
            Text("Ogni bigliettino è un posto in cui tornare.", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text("Cerca tra i tuoi momenti…", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, Modifier.size(20.dp)) }, singleLine = true,
                trailingIcon = { if (query.isNotEmpty()) IconButton({ query = "" }) { Icon(Icons.Outlined.Close, "Svuota ricerca") } },
                shape = RoundedCornerShape(18.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Line, focusedBorderColor = Forest))
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(!favorites, { favorites = false }, { Text("Tutti i momenti", fontSize = 12.sp) }, shape = CircleShape)
                FilterChip(favorites, { favorites = true }, { Text("Del cuore", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Outlined.FavoriteBorder, null, Modifier.size(14.dp)) }, shape = CircleShape)
            }
        }
        if (memories.any { it.date.year == year } && query.isEmpty() && !favorites) item {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFFE9EDDF)).clickable { onYear(year) }.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.MenuBook, null, Modifier.size(23.dp), Forest)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Il tuo $year, in piccoli momenti", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Forest)
                    Text("Apri il racconto del tuo anno", color = Muted, fontSize = 11.sp)
                }
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(17.dp), Forest)
            }
        }
        if (filtered.isEmpty()) item {
            GentleEmpty(if (query.isNotEmpty()) "Ancora da ritrovare." else if (favorites) "I tuoi preferiti, qui." else "Il bello deve ancora arrivare.",
                if (query.isNotEmpty()) "Prova con un’altra parola o un altro anno." else if (favorites) "Tocca il cuore su un ricordo per tenerlo ancora più vicino." else "Il primo bigliettino è l’inizio di una bellissima collezione.",
                Icons.Outlined.Inbox, if (query.isEmpty() && !favorites) "Scrivi il primo momento" else null, onAdd)
        }
        filtered.groupBy { it.date.month }.forEach { (month, notes) ->
            item(key = "month-$month") { Eyebrow("${month.getDisplayName(java.time.format.TextStyle.FULL, Italian)} · ${notes.size} ${if (notes.size == 1) "momento" else "momenti"}", modifier = Modifier.padding(top = 8.dp)) }
            items(notes.chunked(2), key = { it.first().id }) { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEachIndexed { index, memory -> PaperCard(memory, { onOpen(memory) }, Modifier.weight(1f).joyEntrance(index + 1), compact = true) }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun RitualScreen(memories: List<Memory>, reminder: Boolean, time: String, onReminder: (Boolean) -> Unit, onTime: () -> Unit, onExport: () -> Unit, onImport: () -> Unit, onWelcome: () -> Unit) {
    val bottomPadding = floatingContentBottomPadding()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(26.dp, 24.dp, 26.dp, bottomPadding), verticalArrangement = Arrangement.spacedBy(23.dp)) {
        item {
            Eyebrow("UN MINUTO PER TE")
            Spacer(Modifier.height(10.dp))
            Text("Il tuo piccolo rituale.", style = DisplayStyle.copy(fontSize = 33.sp))
            Spacer(Modifier.height(10.dp))
            Text("Non servono giorni perfetti.\nBasta accorgersi di una cosa bella.", color = Muted, fontSize = 14.sp, lineHeight = 23.sp)
        }
        item {
            Row(Modifier.fillMaxWidth().joyEntrance(1).clip(RoundedCornerShape(24.dp)).background(Color(0xFFE8ECDF)).padding(22.dp), horizontalArrangement = Arrangement.SpaceAround) {
                listOf(Triple("${MemoryStats.streak(memories)}", "giorni di fila", Icons.Outlined.LocalFlorist),
                    Triple("${memories.size}", "momenti", Icons.Outlined.StickyNote2),
                    Triple("${memories.count { it.favorite }}", "del cuore", Icons.Outlined.FavoriteBorder)).forEach { (number, label, icon) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, null, Modifier.size(20.dp), Forest)
                        Spacer(Modifier.height(10.dp)); AnimatedCount(number.toInt(), style = DisplayStyle.copy(fontSize = 29.sp))
                        Text(label, color = Muted, fontSize = 11.sp)
                    }
                }
            }
        }
        item {
            Eyebrow("IL TUO APPUNTAMENTO CON IL BELLO")
            Spacer(Modifier.height(12.dp))
            Column(Modifier.joyEntrance(2).clip(RoundedCornerShape(22.dp)).background(Color.White.copy(alpha = .65f)).border(1.dp, Line, RoundedCornerShape(22.dp)).padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.NotificationsNone, null, Modifier.size(23.dp), Forest)
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Un promemoria gentile", fontSize = 14.sp, color = Ink)
                        Text("Ogni giorno, un piccolo invito", color = Muted, fontSize = 11.sp)
                    }
                    Switch(reminder, onReminder, modifier = Modifier.height(36.dp))
                }
                if (reminder) {
                    HorizontalDivider(Modifier.padding(vertical = 14.dp), color = Line)
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(onClick = onTime).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Il momento giusto per te", color = Muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(time, color = Forest, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Icon(Icons.Outlined.ChevronRight, null, Modifier.size(20.dp), Muted)
                    }
                    Text("L’orario può variare leggermente con il risparmio energetico.", color = Muted, fontSize = 10.sp, lineHeight = 16.sp)
                }
            }
        }
        item {
            Eyebrow("CUSTODISCI I TUOI RICORDI")
            Spacer(Modifier.height(12.dp))
            Column(Modifier.joyEntrance(3).clip(RoundedCornerShape(22.dp)).background(Color.White.copy(alpha = .65f)).border(1.dp, Line, RoundedCornerShape(22.dp))) {
                SettingsRow(Icons.Outlined.FileDownload, "Esporta il barattolo", "Un backup da tenere al sicuro", onExport)
                HorizontalDivider(Modifier.padding(horizontal = 18.dp), color = Line)
                SettingsRow(Icons.Outlined.FileUpload, "Importa i tuoi momenti", "Aggiungi i ricordi da un backup", onImport)
                HorizontalDivider(Modifier.padding(horizontal = 18.dp), color = Line)
                SettingsRow(Icons.Outlined.AutoAwesome, "Rivedi il benvenuto", "Ritrova l’inizio di questo piccolo viaggio", onWelcome)
            }
            Spacer(Modifier.height(13.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.Lock, null, Modifier.padding(top = 3.dp).size(13.dp), Muted)
                Spacer(Modifier.width(8.dp))
                Text("Uno spazio solo tuo. I ricordi restano su questo dispositivo. Esporta un backup prima di cambiare telefono o disinstallare l’app.", color = Muted, fontSize = 11.sp, lineHeight = 18.sp)
            }
        }
        item { AppUpdateCard() }
        item {
            Text("“A volte le cose più piccole occupano\npiù spazio nel cuore.”", style = DisplayStyle.copy(fontSize = 20.sp, lineHeight = 29.sp, fontStyle = FontStyle.Italic, color = Forest.copy(alpha = .8f)), modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), textAlign = TextAlign.Center)
            Eyebrow("PICCOLE GIOIE · FATTO PER I GIORNI VERI", modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), color = Muted)
        }
    }
}

@Composable
private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(22.dp), Forest); Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) { Text(title, fontSize = 14.sp, color = Ink); Text(subtitle, color = Muted, fontSize = 11.sp) }
        Icon(Icons.Outlined.ChevronRight, null, Modifier.size(18.dp), Muted)
    }
}
