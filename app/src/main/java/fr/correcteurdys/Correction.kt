package fr.correcteurdys

/** Catégories ergonomiques : LanguageTool ne fournit pas toujours une classe dédiée à la conjugaison. */
enum class Kind(val title: String) { SPELLING("Orthographe"), GRAMMAR("Grammaire et accords"), CONJUGATION("Conjugaison"), PUNCTUATION("Ponctuation") }

data class Correction(val start: Int, val length: Int, val message: String,
                      val suggestions: List<String>, val kind: Kind, val ruleId: String) {
    val end: Int get() = start + length
}

/** La classification est indicative, à partir de l'identifiant et de la catégorie des règles. */
object Classifier {
    fun type(id: String, category: String, issueType: String, message: String): Kind {
        val s = "$id $category $issueType $message".lowercase()
        return when {
            listOf("punct", "virgule", "comma", "apostroph", "typograph", "espace avant").any { it in s } -> Kind.PUNCTUATION
            listOf("conjug", "verb", "verbe", "tense", "temps verbal").any { it in s } -> Kind.CONJUGATION
            issueType.equals("misspelling", true) || "typos" in s || "orthograph" in s -> Kind.SPELLING
            else -> Kind.GRAMMAR
        }
    }
}

/** Les offsets LanguageTool et String Kotlin sont exprimés en unités UTF-16. */
object CorrectionEngine {
    fun valid(text: String, corrections: List<Correction>): List<Correction> = corrections
        .filter { it.start >= 0 && it.length > 0 && it.end <= text.length }
        .sortedWith(compareBy<Correction> { it.start }.thenByDescending { it.length })

    fun applyOne(text: String, correction: Correction, suggestion: String): String =
        if (correction.start < 0 || correction.length <= 0 || correction.end > text.length) text
        else text.replaceRange(correction.start, correction.end, suggestion)

    /** Appliquer de droite à gauche protège les offsets ; les chevauchements sont ignorés. */
    fun applyAll(text: String, corrections: List<Correction>): String {
        var output = text
        var nextStart = text.length
        for (c in valid(text, corrections).asReversed()) {
            val replacement = c.suggestions.firstOrNull() ?: continue
            if (c.end > nextStart) continue
            output = output.replaceRange(c.start, c.end, replacement)
            nextStart = c.start
        }
        return output
    }
}
