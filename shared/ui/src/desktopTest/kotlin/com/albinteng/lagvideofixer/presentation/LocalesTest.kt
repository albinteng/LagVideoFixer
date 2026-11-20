package com.albinteng.lagvideofixer.presentation

import com.albinteng.lagvideofixer.presentation.i18n.Languages
import com.albinteng.lagvideofixer.presentation.i18n.Strings
import com.albinteng.lagvideofixer.presentation.i18n.fill
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalesTest {
    private val dir = File("src/commonMain/composeResources/files/locales")

    private fun read(code: String): Map<String, String> =
        (Json.parseToJsonElement(File(dir, "$code.json").readText()) as JsonObject).mapValues { it.value.jsonPrimitive.content }

    private fun placeholders(s: String) = Regex("\\{\\w+}").findAll(s).map { it.value }.sorted().toList()

    @Test
    fun everyLanguageHasAFileAndEveryFileIsListed() {
        val files = dir.listFiles()!!.map { it.name.removeSuffix(".json") }.toSet()
        assertEquals(Languages.codes.toSet(), files)
    }

    @Test
    fun everyLanguageTranslatesEveryKeyWithTheSamePlaceholders() {
        val en = read("en")
        val problems = Languages.codes.flatMap { code ->
            val t = read(code)
            en.keys.filter { it !in t }.map { "$code: missing $it" } +
                t.keys.filter { it !in en }.map { "$code: unknown $it" } +
                en.keys.filter { it in t && placeholders(en.getValue(it)) != placeholders(t.getValue(it)) }.map { "$code: placeholders of $it" } +
                t.filterValues { it.isBlank() }.keys.map { "$code: empty $it" }
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    /** Messages that point the user to the other mode must name it exactly as the mode picker does. */
    @Test
    fun messagesNameTheModeExactlyAsThePicker() {
        val problems = Languages.codes.flatMap { code ->
            val t = read(code)
            // the picker label is "<name> (<explanation>)", with an ASCII or a full-width parenthesis
            val name = t.getValue("mode_plain").split(Regex("\\s*[(\\uFF08]")).first().trim()
            listOf("err_no_cycle", "err_no_region").filter { name !in t.getValue(it) }.map { "$code: $it does not contain \"$name\"" }
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    @Test
    fun pickerShowsEachLanguageInItsOwnName() = runTest {
        val names = Languages.nativeNames()
        assertEquals(Languages.codes, names.keys.toList())
        assertEquals("English", names["en"])
        assertEquals("Deutsch", names["de"])
        assertTrue(names.all { (code, name) -> name.isNotBlank() && name != code }, names.toString())
    }

    @Test
    fun systemLanguageMapsToASupportedOne() {
        assertEquals("zh-TW", Languages.resolve("auto", "zh-Hant-TW"))
        assertEquals("zh-TW", Languages.resolve("auto", "zh-HK"))
        assertEquals("zh-CN", Languages.resolve("auto", "zh-Hans-CN"))
        assertEquals("zh-CN", Languages.resolve("auto", "zh_SG"))
        assertEquals("pt-PT", Languages.resolve("auto", "pt-PT"))
        assertEquals("pt-BR", Languages.resolve("auto", "pt-BR"))
        assertEquals("pt-BR", Languages.resolve("auto", "pt"))
        assertEquals("sv", Languages.resolve("auto", "sv-SE"))
        assertEquals("hi", Languages.resolve("auto", "hi-IN"))
        assertEquals("id", Languages.resolve("auto", "in-ID"))  // Java's old code for Indonesian
        assertEquals("ja", Languages.resolve("auto", "ja-JP"))
        assertEquals("en", Languages.resolve("auto", "sw-KE"))
        assertEquals("ko", Languages.resolve("ko", "ja-JP"))
        assertEquals("en", Languages.resolve("xx", "en-US"))
    }

    @Test
    fun missingTextFallsBackToEnglishThenTheKey() {
        val s = Strings(mapOf("a" to "A"), mapOf("a" to "x", "b" to "B"))
        assertEquals("A", s["a"])
        assertEquals("B", s["b"])
        assertEquals("c", s["c"])
        assertEquals("3 of 5", "{n} of {total}".fill("n" to 3, "total" to 5))
    }
}
