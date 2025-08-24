package com.dowglasmaia.demouserserviceapi.infrastructure.persistence.jpa

import com.dowglasmaia.demouserserviceapi.application.port.out.UserRepository
import com.dowglasmaia.demouserserviceapi.domain.model.Cpf
import com.dowglasmaia.demouserserviceapi.domain.model.Email
import com.dowglasmaia.demouserserviceapi.domain.model.User
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository
import java.util.*

@Repository
data class UserJpaAdapter(
    private val jpa: SpringUserJpaRepository
) : UserRepository {

    override fun existsByEmail(email: String): Boolean = jpa.existsByEmailIgnoreCase(email)

    override fun existsByCpf(cpf: String): Boolean = jpa.existsByCpf(cpf)

    override fun findById(id: UUID): User? = jpa.findById(id).orElse(null)?.toDomain()

    override fun save(user: User): User {
        val entity = user.toEntity()
        val saved = try {
            jpa.save(entity)
        } catch (e: DataIntegrityViolationException) {
            throw e
        }

        return saved.toDomain()
    }

    private fun User.toEntity() = UserEntity(
        name = this.name,
        email = this.email.value,
        cpf = this.cpf.digits,
        profession = this.profession,
        createdAt = this.createdAt
    )

    private fun UserEntity.toDomain() = User(
        id = this.id?: UUID(0, 0),
        name = this.name,
        email = Email.of(this.email),
        cpf = Cpf.of(this.cpf),
        profession = this.profession,
        createdAt = this.createdAt
    )
}
