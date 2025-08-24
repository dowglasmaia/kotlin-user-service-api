package com.dowglasmaia.demouserserviceapi.infrastructure.persistence.jpa

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_users_email", columnNames = ["email"]),
        UniqueConstraint(name = "uk_users_cpf", columnNames = ["cpf"]),
    ]
)
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, length = 100)
    var name: String = "",

    @Column(nullable = false, length = 120)
    var email: String = "",

    @Column(nullable = false, length = 11)
    var cpf: String = "",

    @Column(nullable = false, length = 100)
    var profession: String = "",

    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
)
