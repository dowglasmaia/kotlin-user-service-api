package com.dowglasmaia.demouserserviceapi.api

import com.dowglasmaia.demouserserviceapi.application.port.`in`.CreateUserDto
import com.dowglasmaia.demouserserviceapi.application.port.`in`.CreateUserUseCase
import com.dowglasmaia.demouserserviceapi.domain.exception.EmailAlreadyUsedException
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [UserController::class])
@Import(ApiExceptionHandler::class)
class UserControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
) {

    @MockitoBean
    private lateinit var createUserUseCase: CreateUserUseCase

    @Test
    @DisplayName("POST /v1/users - 201 Created quando request é válido")
    fun shouldCreateUser() {
        val request = mapOf(
            "name" to "Ana Silva",
            "email" to "ana.silva@example.com",
            "cpf" to "39053344705",
            "profession" to "Software Engineer"
        )

        val output = CreateUserDto(
            id = "3c6b8f5a-3f1d-4a18-bb3c-c3ea2b0f6b9a",
            name = "Ana Silva",
            email = "ana.silva@example.com",
            cpf = "39053344705",
            profession = "Software Engineer",
            createdAt = "2025-08-23T18:27:11Z"
        )

        whenever(createUserUseCase.execute(any())).thenReturn(output)

        mockMvc.perform(
            post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(output.id))
            .andExpect(jsonPath("$.email").value(output.email))
            .andExpect(jsonPath("$.cpf").value(output.cpf))
            .andExpect(jsonPath("$.name").value(output.name))
            .andExpect(jsonPath("$.profession").value(output.profession))
            .andExpect(jsonPath("$.createdAt").value(output.createdAt))
    }

    @Test
    @DisplayName("POST /v1/users - 400 Bad Request quando request é inválido")
    fun shouldReturn400OnInvalidRequest() {
        val bad = mapOf(
            "name" to "",
            "email" to "invalid-email",
            "cpf" to "39053344705",
            "profession" to "Dev"
        )

        mockMvc.perform(
            post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bad))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("validation_error"))
            .andExpect(jsonPath("$.fields").isArray)
    }

    @Test
    @DisplayName("POST /v1/users - 409 Conflict quando e-mail já existe")
    fun shouldReturn409WhenEmailAlreadyUsed() {
        val req = mapOf(
            "name" to "Ana",
            "email" to "ana.silva@example.com",
            "cpf" to "39053344705",
            "profession" to "Dev"
        )

        whenever(createUserUseCase.execute(any()))
            .thenThrow(EmailAlreadyUsedException("ana.silva@example.com"))

        mockMvc.perform(
            post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("conflict"))
            .andExpect(jsonPath("$.message").value("email already used: ana.silva@example.com"))
    }
}