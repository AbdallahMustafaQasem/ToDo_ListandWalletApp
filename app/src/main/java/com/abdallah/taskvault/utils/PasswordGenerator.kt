package com.abdallah.taskvault.utils

object PasswordGenerator {
    private const val LOWER   = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPER   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS  = "0123456789"
    private const val SYMBOLS = "!@#\$%^&*()_+-=[]{}|;:,.<>?"

    fun generate(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val pool = StringBuilder(LOWER)
        if (includeUppercase) pool.append(UPPER)
        if (includeNumbers)   pool.append(DIGITS)
        if (includeSymbols)   pool.append(SYMBOLS)
        if (pool.isEmpty()) return LOWER.take(length)
        return (1..length).map { pool.random() }.joinToString("")
    }

    fun strength(password: String): PasswordStrength {
        var score = 0
        if (password.length >= 8)  score++
        if (password.length >= 12) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() })     score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return when {
            score <= 2 -> PasswordStrength.WEAK
            score <= 4 -> PasswordStrength.MEDIUM
            else       -> PasswordStrength.STRONG
        }
    }
}

enum class PasswordStrength { WEAK, MEDIUM, STRONG }
