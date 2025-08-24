package com.dowglasmaia.demouserserviceapi.domain.model

@JvmInline
value class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "email is required" }
        val normalized = value.trim().lowercase()
        require(EMAIL_REGEX.matches(normalized)) { "email is invalid" }
    }

    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        fun of(raw: String) = Email(raw.trim().lowercase())
    }
}
