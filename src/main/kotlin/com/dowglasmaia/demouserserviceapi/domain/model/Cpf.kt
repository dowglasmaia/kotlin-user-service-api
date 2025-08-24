package com.dowglasmaia.demouserserviceapi.domain.model

@JvmInline
value class Cpf(val digits: String) {
    init {
        require(digits.isNotBlank()) { "cpf is required" }
        val only = digits.filter { it.isDigit() }
        require(only.length == 11) { "cpf must have 11 digits" }
    }

    companion object {
        fun of(raw: String) = Cpf(raw.filter { it.isDigit() })
    }
}
