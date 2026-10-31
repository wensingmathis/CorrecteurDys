package fr.correcteurdys

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val kinds = Kind.entries
private fun tint(kind: Kind) = when (kind) {
    Kind.SPELLING -> Color(0xFFAD5261)
    Kind.GRAMMAR -> Color(0xFF52759B)
    Kind.CONJUGATION -> Color(0xFF7962A0)
    Kind.PUNCTUATION -> Color(0xFF8B7447)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Les préférences ne contiennent jamais le texte rédigé.
        val prefs = getSharedPreferences("accessibility", MODE_PRIVATE)
        val incoming = if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain")
            intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty() else ""
        setContent {
            val vm: EditorViewModel = viewModel()
            LaunchedEffect(incoming) { if (incoming.isNotBlank()) vm.edit(incoming) }
            var settings by remember { mutableStateOf(false) }
            var server by remember { mutableStateOf(prefs.getString("server", "http://192.168.1.100:8081") ?: "") }
            var language by remember { mutableStateOf(prefs.getString("language", "fr") ?: "fr") }
            var dark by remember { mutableStateOf(prefs.getBoolean("dark", false)) }
            var fontSize by remember { mutableFloatStateOf(prefs.getFloat("size", 20f)) }
            var spacing by remember { mutableFloatStateOf(prefs.getFloat("spacing", 0.5f)) }
            var lineHeight by remember { mutableFloatStateOf(prefs.getFloat("line", 1.5f)) }
            var fontChoice by remember { mutableStateOf(prefs.getString("font", "Sans-serif") ?: "Sans-serif") }
            var enabled by remember {
                mutableStateOf(kinds.filter { prefs.getBoolean("kind_${it.name}", true) }.toSet())
            }
            val palette = if (dark) darkColorScheme(background = Color(0xFF1E2025), surface = Color(0xFF292C32), primary = Color(0xFFB9C9DF))
                          else lightColorScheme(background = Color(0xFFF8F7F3), surface = Color(0xFFFFFFFF), primary = Color(0xFF455D7A))
            MaterialTheme(colorScheme = palette) {
                if (settings) {
                    SettingsPage(server, { server = it; prefs.edit().putString("server", it).apply() },
                        language, { language = it; prefs.edit().putString("language", it).apply() },
                        dark, { dark = it; prefs.edit().putBoolean("dark", it).apply() },
                        fontSize, { fontSize = it; prefs.edit().putFloat("size", it).apply() },
                        spacing, { spacing = it; prefs.edit().putFloat("spacing", it).apply() },
                        lineHeight, { lineHeight = it; prefs.edit().putFloat("line", it).apply() },
                        fontChoice, { fontChoice = it; prefs.edit().putString("font", it).apply() },
                        enabled, { kind, checked -> enabled = if (checked) enabled + kind else enabled - kind;
                            prefs.edit().putBoolean("kind_${kind.name}", checked).apply() },
                        { settings = false })
                } else {
                    EditorPage(vm, server, language, enabled, fontSize, spacing, lineHeight, fontChoice,
                        onSettings = { settings = true }, onCopy = {
                            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                                .setPrimaryClip(ClipData.newPlainText("Texte corrigé", vm.text))
                        })
                }
            }
            BackHandler(settings) { settings = false }
        }
    }
}

@Composable private fun EditorPage(vm: EditorViewModel, server: String, language: String,
    enabled: Set<Kind>, size: Float, spacing: Float, line: Float, font: String,
    onSettings: () -> Unit, onCopy: () -> Unit) {
    var field by remember { mutableStateOf(TextFieldValue("")) }
    LaunchedEffect(vm.text) {
        if (field.text != vm.text) field = TextFieldValue(vm.text, TextRange(vm.text.length))
    }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Correcteur Dys", style = MaterialTheme.typography.headlineMedium)
        Text("Tes textes restent sur ton appareil et ton serveur local.", style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = onSettings) { Text("Réglages et serveur") }
        Text("Ton texte", style = MaterialTheme.typography.titleMedium)
        val style = TextStyle(fontSize = size.sp, lineHeight = (size * line).sp,
            letterSpacing = spacing.sp, color = MaterialTheme.colorScheme.onSurface,
            fontFamily = if (font == "Serif") FontFamily.Serif else FontFamily.SansSerif)
        // BasicTextField est modifiable; une sélection dans la prévisualisation ouvre les propositions.
        OutlinedTextField(value = field, onValueChange = { field = it; vm.edit(it.text) },
            textStyle = style, modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
            placeholder = { Text("Écris ou colle ton texte ici…") },
            minLines = 5, label = { Text("Saisie") })
        Button(onClick = { vm.check(server, language, enabled) }, enabled = !vm.busy && vm.text.isNotBlank()) {
            Text(if (vm.busy) "Analyse…" else "Corriger")
        }
        if (vm.status.isNotBlank()) Text(vm.status)
        if (vm.matches.isNotEmpty()) {
            Text("Erreurs soulignées — touche une erreur", style = MaterialTheme.typography.titleMedium)
            val annotated = remember(vm.text, vm.matches) { highlight(vm.text, vm.matches) }
            // Text cliquable avec offsets calculés à partir des annotations (UTF-16).
            @Suppress("DEPRECATION")
            androidx.compose.foundation.text.ClickableText(text = annotated, style = style,
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(14.dp),
                onClick = { offset -> vm.select(vm.matches.firstOrNull { offset in it.start until it.end }) })
            for (kind in kinds) if (vm.matches.any { it.kind == kind }) {
                Text("● ${kind.title}", color = tint(kind), style = MaterialTheme.typography.labelMedium)
            }
            Button(onClick = vm::all, enabled = vm.matches.any { it.suggestions.isNotEmpty() }) { Text("Tout corriger") }
        }
        OutlinedButton(onClick = onCopy, enabled = vm.text.isNotEmpty()) { Text("Copier le texte") }
        Text("Sans compte, sans publicité et sans historique enregistré.", style = MaterialTheme.typography.bodySmall)
    }
    vm.selected?.let { c ->
        AlertDialog(onDismissRequest = { vm.select(null) }, title = { Text(c.kind.title) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(c.message)
                if (c.suggestions.isEmpty()) Text("Aucune proposition automatique.")
                c.suggestions.forEach { suggestion ->
                    OutlinedButton(onClick = { vm.apply(c, suggestion) }, modifier = Modifier.fillMaxWidth()) { Text(suggestion) }
                }
            } }, confirmButton = { TextButton(onClick = { vm.select(null) }) { Text("Fermer") } })
    }
}

private fun highlight(text: String, corrections: List<Correction>): AnnotatedString = buildAnnotatedString {
    append(text)
    corrections.forEach { c ->
        if (c.start >= 0 && c.end <= text.length) {
            addStyle(SpanStyle(color = tint(c.kind), textDecoration = TextDecoration.Underline), c.start, c.end)
        }
    }
}

@Composable private fun SettingsPage(server: String, setServer: (String)->Unit,
    language: String, setLanguage: (String)->Unit, dark: Boolean, setDark: (Boolean)->Unit,
    size: Float, setSize: (Float)->Unit, spacing: Float, setSpacing: (Float)->Unit,
    line: Float, setLine: (Float)->Unit, font: String, setFont: (String)->Unit,
    enabled: Set<Kind>, setKind: (Kind, Boolean)->Unit, onBack: ()->Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Réglages", style = MaterialTheme.typography.headlineMedium)
        OutlinedButton(onClick = onBack) { Text("Retour au texte") }
        Text("Serveur LanguageTool sur ton réseau local")
        OutlinedTextField(value = server, onValueChange = setServer, label = { Text("Adresse du serveur") },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Text("Exemple : http://192.168.1.100:8081. Aucune adresse Internet publique n'est acceptée.", style = MaterialTheme.typography.bodySmall)
        Text("Langue")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("fr", "en-US", "de-DE").forEach { lang -> FilterChip(selected = language == lang,
                onClick = { setLanguage(lang) }, label = { Text(lang) }) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Mode sombre"); Switch(checked = dark, onCheckedChange = setDark)
        }
        Text("Taille du texte : ${size.toInt()} sp")
        Slider(value = size, onValueChange = setSize, valueRange = 16f..32f)
        Text("Espacement des lettres : ${"%.1f".format(spacing)} sp")
        Slider(value = spacing, onValueChange = setSpacing, valueRange = 0f..3f)
        Text("Interligne : ${"%.1f".format(line)} ×")
        Slider(value = line, onValueChange = setLine, valueRange = 1.1f..2.2f)
        Text("Police")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Sans-serif", "Serif").forEach { choice -> FilterChip(selected = font == choice,
                onClick = { setFont(choice) }, label = { Text(choice) }) }
        }
        Text("Catégories à détecter", style = MaterialTheme.typography.titleMedium)
        kinds.forEach { kind -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(kind.title, color = tint(kind)); Switch(checked = kind in enabled, onCheckedChange = { setKind(kind, it) })
        } }
        Text("La classification est indicative : les règles LanguageTool ne correspondent pas toujours exactement aux quatre catégories.", style = MaterialTheme.typography.bodySmall)
    }
}
