package com.dowglasmaia.demouserserviceapi.application.port.out

import com.dowglasmaia.demouserserviceapi.domain.model.User
import java.util.UUID

interface UserRepository {
    fun existsByEmail(email: String): Boolean
    fun existsByCpf(cpf: String) : Boolean
    fun save(user: User): User
    fun findById(id: UUID): User?
}