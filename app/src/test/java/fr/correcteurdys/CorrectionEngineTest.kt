package fr.correcteurdys

import org.junit.Assert.*
import org.junit.Test

class CorrectionEngineTest {
    @Test fun applyOne() {
        assertEquals("Bonjour le monde", CorrectionEngine.applyOne("Bonjor le monde",
            Correction(0, 6, "", listOf("Bonjour"), Kind.SPELLING, "id"), "Bonjour"))
    }
    @Test fun allUsesOriginalOffsets() {
        val text = "Bonjor et mercis"
        val corrections = listOf(
            Correction(0, 6, "", listOf("Bonjour"), Kind.SPELLING, "a"),
            Correction(10, 6, "", listOf("merci"), Kind.SPELLING, "b"))
        assertEquals("Bonjour et merci", CorrectionEngine.applyAll(text, corrections))
    }
    @Test fun ignoresInvalidAndOverlaps() {
        val text = "abcdef"
        val corrections = listOf(Correction(-1, 1, "", listOf("X"), Kind.GRAMMAR, "x"),
            Correction(0, 4, "", listOf("A"), Kind.GRAMMAR, "a"),
            Correction(2, 3, "", listOf("B"), Kind.GRAMMAR, "b"))
        assertEquals("abBf", CorrectionEngine.applyAll(text, corrections))
    }
    @Test fun rejectsPublicEndpoints() {
        assertEquals("http://192.168.1.100:8081", LocalAddress.validated("http://192.168.1.100:8081/"))
        for (host in listOf("https://api.languagetool.org:443", "http://8.8.8.8:8081", "http://example.com:8081")) {
            try { LocalAddress.validated(host); fail("Expected failure for $host") } catch (_: IllegalArgumentException) { }
        }
    }
    @Test fun categorizes() {
        assertEquals(Kind.CONJUGATION, Classifier.type("VERB_TENSE", "GRAMMAR", "", ""))
        assertEquals(Kind.SPELLING, Classifier.type("MORFOLOGIK_RULE_FR", "", "misspelling", ""))
        assertEquals(Kind.PUNCTUATION, Classifier.type("COMMA", "", "", ""))
    }
}
