package it.piccolegioie.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppUpdateCard(vm: AppUpdateViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Column {
        Eyebrow("PICCOLE NOVITÀ")
        Spacer(Modifier.height(12.dp))
        Column(Modifier.fillMaxWidth().animateContentSize().clip(RoundedCornerShape(22.dp))
            .background(SoftSurface.copy(alpha = .65f)).border(1.dp, Line, RoundedCornerShape(22.dp)).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(25.dp), Gold)
                Spacer(Modifier.width(13.dp))
                Column {
                    Text("Il bello continua a crescere.", style = DisplayStyle.copy(fontSize = 21.sp, lineHeight = 26.sp))
                    Text("La tua versione · ${BuildConfig.VERSION_NAME}", color = Muted, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            AnimatedContent(state.phase, label = "update status") { phase ->
                Column(Modifier.fillMaxWidth()) {
                    val description = when (phase) {
                        UpdatePhase.Idle -> "Un piccolo gesto per avere le ultime novità."
                        UpdatePhase.Checking -> "Cerco nuove gioie su GitHub…"
                        UpdatePhase.Current -> "Hai già tutte le ultime novità."
                        UpdatePhase.Available -> "È arrivata la versione ${state.release?.versionName}. I tuoi ricordi ti seguiranno nell’aggiornamento."
                        UpdatePhase.Downloading -> "Preparo la versione ${state.release?.versionName}… ${(state.progress * 100).toInt()}%"
                        UpdatePhase.Ready -> state.message.ifEmpty { "Tutto pronto. Android ti chiederà di confermare l’installazione." }
                        UpdatePhase.Error -> state.message
                    }
                    Text(description, color = Muted, fontSize = 12.sp, lineHeight = 19.sp)
                    Spacer(Modifier.height(14.dp))
                    when (phase) {
                        UpdatePhase.Checking -> LinearProgressIndicator(Modifier.fillMaxWidth(), color = Gold, trackColor = Line)
                        UpdatePhase.Downloading -> LinearProgressIndicator(progress = { state.progress }, Modifier.fillMaxWidth(), color = Gold, trackColor = Line)
                        UpdatePhase.Available -> PrimaryButton("Scarica aggiornamento", Icons.Outlined.Download, onClick = vm::download)
                        UpdatePhase.Ready -> PrimaryButton("Installa aggiornamento", Icons.Outlined.SystemUpdate, onClick = { vm.install(context) })
                        else -> OutlinedButton(vm::check, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Icon(Icons.Outlined.Refresh, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (phase == UpdatePhase.Error) "Riprova" else "Cerca aggiornamenti", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Solo per gli aggiornamenti ci colleghiamo a GitHub. I tuoi momenti restano sul dispositivo.", color = Muted, fontSize = 10.sp, lineHeight = 16.sp)
    }
}
