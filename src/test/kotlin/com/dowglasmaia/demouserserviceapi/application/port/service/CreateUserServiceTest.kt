package com.dowglasmaia.demouserserviceapi.application.port.service
import com.dowglasmaia.demouserserviceapi.application.port.`in`.CreateUserCommand
import com.dowglasmaia.demouserserviceapi.application.port.out.UserRepository
import com.dowglasmaia.demouserserviceapi.domain.exception.CpfAlreadyUsedException
import com.dowglasmaia.demouserserviceapi.domain.exception.EmailAlreadyUsedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset


class CreateUserServiceTest{

    private val repository: UserRepository = mock()

    private val fixedInstant = Instant.parse("2025-08-23T18:27:11Z")
    private val clock: Clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

    private val service = CreateUserService(repository, clock)

    @Test
    @DisplayName("execute() -> creates user when email and CPF are unique")
    fun shouldCreateUserSuccessfully() {
        // arrange
        val cmd = CreateUserCommand(
            name = "Ana Silva",
            email = "Ana.Silva@Example.com", // will be normalized to lowercase
            cpf = "390.533.447-05",          // will be normalized to digits-only
            profession = "Software Engineer"
        )

        whenever(repository.existsByEmail("ana.silva@example.com")).thenReturn(false)
        whenever(repository.existsByCpf("39053344705")).thenReturn(false)

        // repository.save returns the same domain object (enough for this unit test)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] }

        // act
        val out = service.execute(cmd)

        // assert — payload
        assertEquals("Ana Silva", out.name)
        assertEquals("ana.silva@example.com", out.email)   // normalized email
        assertEquals("39053344705", out.cpf)               // digits-only CPF
        assertEquals("Software Engineer", out.profession)
        assertEquals("2025-08-23T18:27:11Z", out.createdAt) // fixed clock

        // assert — interactions
        verify(repository).existsByEmail("ana.silva@example.com")
        verify(repository).existsByCpf("39053344705")       // will fail if service calls existsByEmail(cpf)
        verify(repository).save(any())

        // optional: capture the saved domain object to assert normalizations and timestamp there too
        val userCaptor = argumentCaptor<com.dowglasmaia.demouserserviceapi.domain.model.User>()
        verify(repository).save(userCaptor.capture())
        val saved = userCaptor.firstValue
        assertEquals("ana.silva@example.com", saved.email.value)
        assertEquals("39053344705", saved.cpf.digits)
        assertEquals(fixedInstant, saved.createdAt)
    }

    @Test
    @DisplayName("execute() -> throws EmailAlreadyUsedException when email already exists")
    fun shouldThrowWhenEmailAlreadyUsed() {
        // arrange
        val cmd = CreateUserCommand(
            name = "Ana",
            email = "ana@example.com",
            cpf = "39053344705",
            profession = "Dev"
        )

        whenever(repository.existsByEmail("ana@example.com")).thenReturn(true)

        // act & assert
        assertThrows(EmailAlreadyUsedException::class.java) {
            service.execute(cmd)
        }

        // verify interactions
        verify(repository).existsByEmail("ana@example.com")
        verify(repository, never()).existsByCpf(any())   // CPF should not even be checked
        verify(repository, never()).save(any())          // save must not be called
    }

    @Test
    @DisplayName("execute() -> throws CpfAlreadyUsedException when CPF already exists")
    fun shouldThrowWhenCpfAlreadyUsed() {
        // arrange
        val cmd = CreateUserCommand(
            name = "Ana",
            email = "ana@example.com",
            cpf = "39053344705",
            profession = "Dev"
        )

        whenever(repository.existsByEmail("ana@example.com")).thenReturn(false)
        whenever(repository.existsByCpf("39053344705")).thenReturn(true)

        // act & assert
        assertThrows(CpfAlreadyUsedException::class.java) {
            service.execute(cmd)
        }

        // verify interactions
        verify(repository).existsByEmail("ana@example.com")
        verify(repository).existsByCpf("39053344705")   // will fail if service wrongly calls existsByEmail(cpf)
        verify(repository, never()).save(any())         // save must not be called
    }
}