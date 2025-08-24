package com.dowglasmaia.demouserserviceapi.api


import com.dowglasmaia.demouserserviceapi.application.port.`in`.CreateUserCommand
import com.dowglasmaia.demouserserviceapi.application.port.`in`.CreateUserUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users")
data class UserController(
    private val createUser: CreateUserUseCase
) {

    @PostMapping
    fun create(
        @Valid @RequestBody req: CreateUserRequest
    ): ResponseEntity<UserResponse> {
        val out = createUser.execute(
            CreateUserCommand(
                name = req.name,
                email = req.email,
                cpf = req.cpf,
                profession = req.profession
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(
            UserResponse(
                id = out.id,
                name = out.name,
                email = out.email,
                cpf = out.cpf,
                profession = out.profession,
                createdAt = out.createdAt
            )
        )
    }
}
