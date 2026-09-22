package com.example.commandcenter.data

data class Rule(
    val id: Long,
    val source: String,
    val replacement: String,
    val enabled: Boolean = true,
    val exactMatch: Boolean = false,
    val caseSensitive: Boolean = false
)
