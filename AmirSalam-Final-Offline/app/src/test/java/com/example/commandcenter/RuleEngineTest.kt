package com.example.commandcenter

import com.example.commandcenter.data.Rule
import com.example.commandcenter.domain.RuleEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class RuleEngineTest {
    @Test fun defaultReplacementWorks() {
        val rules = listOf(
            Rule(1, "تنظیمات", "امیر.سلام"),
            Rule(2, "تلفن", "مزاحمت"),
            Rule(3, "برنامه‌ها", "سلاح‌ها")
        )
        assertEquals(
            "امیر.سلام مزاحمت و سلاح‌ها",
            RuleEngine.replace("تنظیمات تلفن و برنامه‌ها", rules)
        )
    }

    @Test fun arabicLettersNormalize() {
        assertEquals("کی", RuleEngine.normalize("كي"))
    }
}
