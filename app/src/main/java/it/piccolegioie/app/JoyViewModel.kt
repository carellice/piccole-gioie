package it.piccolegioie.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class JoyViewModel(application: Application) : AndroidViewModel(application) {
    private val store = MemoryStore(application)
    private val lock = Mutex()
    private val _memories = MutableStateFlow<List<Memory>>(emptyList())
    val memories = _memories.asStateFlow()
    private val _ready = MutableStateFlow(false)
    val ready = _ready.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        viewModelScope.launch {
            try { _memories.value = withContext(Dispatchers.IO) { store.read() }; _ready.value = true }
            catch (_: Exception) { _error.value = "Non riesco a leggere i ricordi. I dati originali sono al sicuro: riapri l’app per riprovare." }
        }
    }

    fun dismissError() { _error.value = null }
    fun add(memory: Memory, onSaved: () -> Unit, onFailed: () -> Unit) = change(onSaved, onFailed) { listOf(memory) + it }
    fun favorite(memory: Memory) = change { list -> list.map { if (it.id == memory.id) it.copy(favorite = !it.favorite) else it } }
    fun delete(memory: Memory, onDeleted: () -> Unit) = change(onDeleted) { list -> list.filterNot { it.id == memory.id } }
    fun restore(json: String, onRestored: () -> Unit) {
        viewModelScope.launch {
            try {
                val imported = withContext(Dispatchers.IO) { MemoryStore.decode(json) }
                change(onRestored) { existing -> (existing + imported).distinctBy { it.id }.sortedByDescending { it.createdAt } }
            } catch (_: Exception) { _error.value = "Questo file non è un backup valido di Piccole gioie." }
        }
    }

    private fun change(onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}, transform: (List<Memory>) -> List<Memory>) {
        viewModelScope.launch {
            lock.withLock {
                if (!_ready.value) { onFailure(); return@withLock }
                try {
                    val next = transform(_memories.value)
                    withContext(Dispatchers.IO) { store.save(next) }
                    _memories.value = next
                    onSuccess()
                } catch (_: Exception) { onFailure(); _error.value = "Il salvataggio non è riuscito. Il tuo bigliettino è ancora qui: riprova." }
            }
        }
    }
}
