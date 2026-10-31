package fr.correcteurdys

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class EditorViewModel : ViewModel() {
    var text by mutableStateOf(""); private set
    var matches by mutableStateOf<List<Correction>>(emptyList()); private set
    var selected by mutableStateOf<Correction?>(null); private set
    var busy by mutableStateOf(false); private set
    var status by mutableStateOf(""); private set
    private val client = LanguageToolClient()

    fun edit(value: String) { text = value; matches = emptyList(); selected = null; status = "" }
    fun select(c: Correction?) { selected = c }
    fun check(server: String, language: String, enabled: Set<Kind>) {
        if (busy || text.isBlank()) return
        val snapshot = text
        busy = true; selected = null; status = ""
        viewModelScope.launch {
            try {
                val found = client.check(snapshot, server, language)
                if (snapshot == text) {
                    matches = CorrectionEngine.valid(text, found).filter { it.kind in enabled }
                    status = "${matches.size} problème(s) détecté(s)."
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { status = "Erreur : ${e.message ?: "connexion impossible"}" }
            finally { busy = false }
        }
    }
    fun apply(c: Correction, replacement: String) {
        edit(CorrectionEngine.applyOne(text, c, replacement))
        status = "Correction appliquée. Relance une analyse pour vérifier le texte."
    }
    fun all() {
        edit(CorrectionEngine.applyAll(text, matches))
        status = "Suggestions appliquées. Relance une analyse pour vérifier le texte."
    }
}
