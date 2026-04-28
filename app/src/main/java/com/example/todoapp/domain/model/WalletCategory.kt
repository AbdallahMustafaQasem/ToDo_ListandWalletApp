package com.example.todoapp.domain.model

data class WalletCategory(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val isDefault: Boolean = false
)
