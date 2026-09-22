package com.example.commandcenter.domain

import com.example.commandcenter.data.Rule
import java.util.Locale

object RuleEngine {
    fun normalize(text: String): String =
        text.replace('ي', 'ی')
            .replace('ى', 'ی')
            .replace('ك', 'ک')
            .replace('\u0640'.toString(), "")
            .replace(Regex("[\\u200C\\u200D]"), "\u200C")
            .replace(Regex("\\s+"), " ")

    fun replace(text: String, rules: List<Rule>): String {
        var result = text
        rules.filter { it.enabled && it.source.isNotBlank() }
            .sortedByDescending { it.source.length }
            .forEach { rule ->
                val source = if (rule.caseSensitive) rule.source else normalize(rule.source)
                val input = if (rule.caseSensitive) result else normalize(result)
                if (rule.exactMatch) {
                    if (input == source) result = rule.replacement
                } else {
                    val escaped = Regex.escape(source)
                    val pattern = if (containsPersianOrArabic(source))
                        "(?<![\\p{L}\\p{N}])$escaped(?![\\p{L}\\p{N}])"
                    else
                        "(?<![A-Za-z0-9_])$escaped(?![A-Za-z0-9_])"
                    result = Regex(pattern, if (rule.caseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE))
                        .replace(result, rule.replacement)
                }
            }
        return result
    }

    private fun containsPersianOrArabic(s: String) =
        s.any { it in '\u0600'..'\u06FF' }
}
