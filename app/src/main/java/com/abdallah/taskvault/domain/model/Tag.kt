package com.abdallah.taskvault.domain.model

data class Tag(
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#6750A4"
)
