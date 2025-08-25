package com.dowglasmaia.demouserserviceapi.application.port.service

import com.dowglasmaia.demouserserviceapi.application.port.`in`.CreateUserCommand
import com.dowglasmaia.demouserserviceapi.application.port.`in`.CreateUserDto
import com.dowglasmaia.demouserserviceapi.application.port.`in`.CreateUserUseCase
import com.dowglasmaia.demouserserviceapi.application.port.out.UserRepository
import com.dowglasmaia.demouserserviceapi.domain.exception.CpfAlreadyUsedException
import com.dowglasmaia.demouserserviceapi.domain.exception.EmailAlreadyUsedException
import com.dowglasmaia.demouserserviceapi.domain.model.User
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.format.DateTimeFormatter

@Service
class CreateUserService(
    private val repository: UserRepository,
    private val clock: Clock = Clock.systemUTC()
) : CreateUserUseCase {

    @Transactional
    override fun execute(cmd: CreateUserCommand): CreateUserDto {
        val user = User.create(
            name = cmd.name,
            email = cmd.email,
            cpf = cmd.cpf,
            profession = cmd.profession,
            now = java.time.Instant.now(clock)
        )

        if (repository.existsByEmail(user.email.value)) throw EmailAlreadyUsedException(user.email.value)
        if (repository.existsByCpf(user.cpf.digits)) throw CpfAlreadyUsedException(user.cpf.digits)

        val userSaved = repository.save(user)
        return CreateUserDto(
            id = userSaved.id.toString(),
            name = userSaved.name,
            email = userSaved.email.value,
            cpf = userSaved.cpf.digits,
            profession = userSaved.profession,
            createdAt = DateTimeFormatter.ISO_INSTANT.format(userSaved.createdAt)
        )
    }
}
