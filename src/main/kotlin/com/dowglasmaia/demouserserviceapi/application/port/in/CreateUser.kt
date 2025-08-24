package com.dowglasmaia.demouserserviceapi.application.port.`in`

interface CreateUserUseCase {
    fun execute(cmd: CreateUserCommand): CreateUserDto
}

data class CreateUserCommand(
    val name: String,
    val email: String,
    val cpf: String,
    val profession: String
)

data class CreateUserDto(
    val id: String,
    val name: String,
    val email: String,
    val cpf: String,
    val profession: String,
    val createdAt: String
)
