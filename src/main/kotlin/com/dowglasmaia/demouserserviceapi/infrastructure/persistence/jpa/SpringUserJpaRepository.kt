package com.dowglasmaia.demouserserviceapi.infrastructure.persistence.jpa


import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SpringUserJpaRepository : JpaRepository<UserEntity, UUID> {
    fun existsByEmailIgnoreCase(email: String): Boolean
    fun existsByCpf(cpf: String): Boolean
}
