package com.dowglasmaia.demouserserviceapi.domain.model

import java.time.Instant
import java.util.*

data class User(
    var id: UUID,
    var name: String,
    var email: Email,
    var cpf: Cpf,
    var profession: String,
    var createdAt: Instant
) {
    companion object {
        fun create(
            name: String,
            email: String,
            cpf: String,
            profession: String,
            now: Instant
        ): User = User(
            id = UUID.randomUUID(),
            name = name.trim(),
            email = Email.of(email),
            cpf = Cpf.of(cpf),
            profession = profession.trim(),
            createdAt = now
        )
    }
}
