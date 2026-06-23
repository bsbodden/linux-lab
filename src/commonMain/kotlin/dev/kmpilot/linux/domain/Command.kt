package dev.kmpilot.linux.domain

/** A Linux command reference entry (the domain model — a framework-free POJO). */
data class Command(
    val name: String,
    val category: String,
    val summary: String,
    val syntax: String,
    val examples: List<String>,
)
