package com.dowglasmaia.demouserserviceapi.api

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.br.CPF

data class CreateUserRequest(

    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "name must be between 2 and 100 chars")
    val name: String,

    @field:NotBlank(message = "email is required")
    @field:Email(message = "email must be valid")
    val email: String,

    @field:NotBlank(message = "CPF is required")
    @field:CPF(message = "CPF must be valid")
    val cpf: String,

    @field:NotBlank(message = "profession is required")
    @field:Size(min = 3, max = 100, message = "profession must be between 2 and 100 chars")
    val profession: String
)

data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val cpf: String,
    val profession: String,
    val createdAt: String
)
