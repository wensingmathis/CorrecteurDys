package fr.correcteurdys

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.InetAddress
import java.net.URL
import java.net.URLEncoder

/** Une URL en IPv4 privée uniquement : pas de DNS, ni Internet, ni redirection. */
object LocalAddress {
    fun validated(value: String): String {
        val raw = value.trim().removeSuffix("/")
        val match = Regex("^http://([0-9.]+):([0-9]{1,5})$").matchEntire(raw)
            ?: throw IllegalArgumentException("Utilise http://192.168.x.x:8081 (serveur sur ton réseau local).")
        val host = match.groupValues[1]
        val pieces = host.split('.')
        require(pieces.size == 4 && pieces.all { it.isNotEmpty() && it.length <= 3 && it.all(Char::isDigit) && it.toInt() in 0..255 }) { "Adresse IPv4 invalide" }
        val a = pieces.map { it.toInt() }
        val allowed = a[0] == 10 || (a[0] == 172 && a[1] in 16..31) || (a[0] == 192 && a[1] == 168) || a[0] == 127
        require(allowed) { "Seules les adresses privées/locales sont autorisées." }
        val port = match.groupValues[2].toInt()
        require(port in 1..65535) { "Port invalide" }
        return "http://${a.joinToString(".")}:$port"
    }
}

class LanguageToolClient {
    suspend fun check(text: String, server: String, language: String): List<Correction> = withContext(Dispatchers.IO) {
        val base = LocalAddress.validated(server)
        require(text.length <= 20000) { "Texte trop long (20 000 caractères maximum)." }
        require(language.matches(Regex("[a-z]{2}(-[A-Za-z]{2,4})?"))) { "Langue invalide" }
        val url = URL("$base/v2/check")
        val conn = (url.openConnection() as HttpURLConnection)
        try {
            conn.requestMethod = "POST"
            conn.instanceFollowRedirects = false
            conn.connectTimeout = 5000
            conn.readTimeout = 20000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            val body = "language=${URLEncoder.encode(language, "UTF-8")}&text=${URLEncoder.encode(text, "UTF-8")}"
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            require(conn.responseCode == 200) { "Serveur indisponible (HTTP ${conn.responseCode})." }
            val json = JSONObject(conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() })
            val matches = json.optJSONArray("matches") ?: return@withContext emptyList()
            buildList {
                for (i in 0 until matches.length()) {
                    val item = matches.getJSONObject(i)
                    val rule = item.optJSONObject("rule") ?: JSONObject()
                    val category = rule.optJSONObject("category")?.optString("id").orEmpty()
                    val replacements = item.optJSONArray("replacements")
                    val suggestions = buildList {
                        if (replacements != null) for (j in 0 until minOf(replacements.length(), 5)) {
                            val value = replacements.getJSONObject(j).optString("value")
                            if (value.isNotEmpty() && value !in this) add(value)
                        }
                    }
                    val c = Correction(item.optInt("offset", -1), item.optInt("length", 0),
                        item.optString("message", "Correction suggérée"), suggestions,
                        Classifier.type(rule.optString("id"), category, item.optString("ruleIssueType"), item.optString("message")),
                        rule.optString("id"))
                    add(c)
                }
            }
        } finally { conn.disconnect() }
    }
}
