package it.piccolegioie.app

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(0xFFF9F6EF.toInt(), 0xFFF9F6EF.toInt()),
            navigationBarStyle = SystemBarStyle.light(0xFFF9F6EF.toInt(), 0xFFF9F6EF.toInt()))
        setContent { JoyTheme { JoyApp() } }
    }
}

@Composable
fun JoyApp(vm: JoyViewModel = viewModel()) {
    val context = LocalContext.current
    val store = remember { MemoryStore(context) }
    var welcome by rememberSaveable { mutableStateOf(!store.onboardingCompleted) }
    AnimatedContent(welcome, Modifier.fillMaxSize(), transitionSpec = {
        (fadeIn(tween(550, delayMillis = 100)) + scaleIn(tween(550), initialScale = .96f)) togetherWith fadeOut(tween(320))
    }, label = "welcome to your jar") { showingWelcome ->
        if (showingWelcome) OnboardingScreen {
            store.onboardingCompleted = true
            welcome = false
        } else JoyMainApp(vm, onWelcome = { welcome = true })
    }
}

@Composable
private fun JoyMainApp(vm: JoyViewModel, onWelcome: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    val memories by vm.memories.collectAsStateWithLifecycle()
    val ready by vm.ready.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var writing by rememberSaveable { mutableStateOf(false) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var random by rememberSaveable { mutableStateOf(false) }
    var year by rememberSaveable { mutableStateOf<Int?>(null) }
    var celebration by remember { mutableIntStateOf(0) }
    val store = remember { MemoryStore(context) }
    var reminder by remember { mutableStateOf(store.reminderEnabled && Reminders.allowed(context)) }
    var hour by remember { mutableIntStateOf(store.reminderHour) }
    var minute by remember { mutableIntStateOf(store.reminderMinute) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    fun message(text: String) { scope.launch { snack.showSnackbar(text) } }

    LaunchedEffect(error) { error?.let { message(it); vm.dismissError() } }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                reminder = store.reminderEnabled && Reminders.allowed(context)
                Reminders.schedule(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        reminder = granted && Reminders.allowed(context); store.reminderEnabled = reminder; Reminders.schedule(context)
        if (!reminder) message("Puoi consentire le notifiche nelle impostazioni Android quando vuoi.")
    }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            val snapshot = memories
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        requireNotNull(context.contentResolver.openOutputStream(uri, "wt")).bufferedWriter().use { it.write(MemoryStore.encode(snapshot)) }
                    }
                    message("Il tuo barattolo è stato esportato.")
                } catch (_: Exception) { message("Esportazione non riuscita. Scegli un’altra posizione e riprova.") }
            }
        }
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    requireNotNull(context.contentResolver.openInputStream(uri)).use { stream ->
                        val result = java.io.ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        var count = stream.read(buffer)
                        while (count != -1) {
                            require(result.size() + count <= 16 * 1024 * 1024)
                            result.write(buffer, 0, count)
                            count = stream.read(buffer)
                        }
                        result.toString("UTF-8")
                    }
                }
                vm.restore(json) { message("I tuoi momenti sono nel barattolo. I duplicati sono stati ignorati.") }
            } catch (_: Exception) { message("Non riesco a leggere questo backup. Scegli un file di Piccole gioie fino a 16 MB.") }
        }
    }
    fun fish() {
        val candidates = memories.filterNot { it.id == selectedId }.ifEmpty { memories }
        if (candidates.isEmpty()) {
            message("Il primo sorriso lo scrivi tu. Aggiungi un momento al barattolo.")
        } else {
            random = true; selectedId = candidates.random().id
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    BackHandler(tab != 0 && !writing && selectedId == null && year == null) { tab = 0 }
    Box(Modifier.fillMaxSize()) {
    Scaffold(containerColor = Cream,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        snackbarHost = { SnackbarHost(snack, Modifier.padding(bottom = if (keyboardVisible) 0.dp else floatingContentBottomPadding())) }) { padding ->
        AnimatedContent(tab, Modifier.padding(padding).imePadding().fillMaxSize().clipToBounds(), transitionSpec = {
            val direction = if (targetState > initialState) 1 else -1
            (fadeIn(tween(360, delayMillis = 65)) + slideInHorizontally(tween(450)) { direction * it / 7 } + scaleIn(tween(450), initialScale = .965f)) togetherWith
                (fadeOut(tween(180)) + slideOutHorizontally(tween(360)) { -direction * it / 10 })
        }, label = "page") { current ->
            when (current) {
                0 -> HomeScreen(memories, ready, { writing = true }, ::fish, { random = false; selectedId = it.id }, { tab = 1 }, { year = LocalDate.now().year })
                1 -> ArchiveScreen(memories, { random = false; selectedId = it.id }, { writing = true }, { year = it })
                else -> RitualScreen(memories, reminder, "%02d:%02d".format(hour, minute), { enabled ->
                    if (enabled && Build.VERSION.SDK_INT >= 33 && !Reminders.allowed(context)) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else {
                        reminder = enabled && Reminders.allowed(context); store.reminderEnabled = reminder; Reminders.schedule(context)
                        if (enabled && !reminder) message("Consenti le notifiche dalle impostazioni Android per attivare il promemoria.")
                    }
                }, {
                    TimePickerDialog(context, { _, h, m -> hour = h; minute = m; store.reminderHour = h; store.reminderMinute = m; Reminders.schedule(context) }, hour, minute, true).show()
                }, { export.launch("piccole-gioie-${LocalDate.now()}.json") }, { import.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }, onWelcome)
            }
        }
    }
    AnimatedVisibility(!keyboardVisible, Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(tween(300)) { it } + fadeIn(),
        exit = slideOutVertically(tween(200)) { it } + fadeOut()) {
        CozyDock(tab, { tab = it })
    }
    JoyCelebration(celebration, Modifier.matchParentSize())
    }
    if (writing) WriteSheet({ writing = false }) { memory, reset ->
        vm.add(memory, {
            reset(); writing = false; tab = 0; celebration++
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            message("Un altro pezzetto di felicità, al sicuro.")
        }, reset)
    }
    memories.find { it.id == selectedId }?.let { selected ->
        MemorySheet(selected, random, { selectedId = null }, { vm.favorite(selected) },
            { vm.delete(selected) { selectedId = null; message("Il ricordo è stato eliminato.") } }, ::fish)
    }
    year?.let { y -> YearSheet(y, memories, { year = null }, { year = null; random = false; selectedId = it.id }) }
}
