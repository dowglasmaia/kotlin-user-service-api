package com.dowglasmaia.demouserserviceapi.api


import com.dowglasmaia.demouserserviceapi.domain.exception.CpfAlreadyUsedException
import com.dowglasmaia.demouserserviceapi.domain.exception.EmailAlreadyUsedException
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.Clock
import java.time.Instant
import java.time.format.DateTimeFormatter

@RestControllerAdvice
class ApiExceptionHandler(
    private val clock: Clock = Clock.systemUTC()
) : ResponseEntityExceptionHandler() {

    data class FieldError(val name: String, val reason: String)
    data class ErrorResponse(
        val timestamp: String,
        val path: String?,
        val status: Int,
        val error: String,
        val message: String?,
        val fields: List<FieldError>? = null
    )

    private fun now() = DateTimeFormatter.ISO_INSTANT.format(Instant.now(clock))
    private fun pathOf(req: WebRequest): String? =
        (req as? ServletWebRequest)?.request?.requestURI

    private fun build(
        status: HttpStatus,
        req: WebRequest,
        error: String,
        message: String?,
        fields: List<FieldError>? = null
    ) = ResponseEntity.status(status).body(
        ErrorResponse(
            timestamp = now(),
            path = pathOf(req),
            status = status.value(),
            error = error,
            message = message,
            fields = fields
        )
    )

    // 400 - Bean Validation on @RequestBody (ex.: CreateUserRequest)
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        req: WebRequest
    ): ResponseEntity<Any> {
        val fields = ex.bindingResult.fieldErrors.map {
            FieldError(it.field, it.defaultMessage ?: "invalid")
        }
        val body = ErrorResponse(
            timestamp = now(),
            path = pathOf(req),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "validation_error",
            message = "Validation failed",
            fields = fields
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    // 400 — Bad Request: malformed JSON or invalid value type
    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        req: WebRequest
    ): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                timestamp = now(),
                path = pathOf(req),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "malformed_json",
                message = ex.mostSpecificCause?.message ?: "Request body is invalid"
            )
        )

    // 400 — Bad Request: missing required query/form parameter
    override fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        req: WebRequest
    ): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                timestamp = now(),
                path = pathOf(req),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "missing_parameter",
                message = "Missing parameter: ${ex.parameterName}"
            )
        )

    // 400 - Constraint em @PathVariable / @RequestParam (@Validated)
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException, req: WebRequest): ResponseEntity<ErrorResponse> {
        val fields = ex.constraintViolations.map { v ->
            FieldError(v.propertyPath.toString(), v.message)
        }
        return build(HttpStatus.BAD_REQUEST, req, "constraint_violation", "Validation failed", fields)
    }

    // 409 — Conflict (business): email/CPF already in use
    @ExceptionHandler(EmailAlreadyUsedException::class, CpfAlreadyUsedException::class)
    fun handleBusinessConflict(ex: RuntimeException, req: WebRequest): ResponseEntity<ErrorResponse> =
        build(HttpStatus.CONFLICT, req, "conflict", ex.message)

    // 409 — Conflict (DB guard): unique constraint violation that slipped past the service layer
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleUniqueConstraint(ex: DataIntegrityViolationException, req: WebRequest): ResponseEntity<ErrorResponse> =
        build(HttpStatus.CONFLICT, req, "constraint_violation", ex.rootCause?.message ?: ex.message)

    // 404 — Not Found: thrown when a resource is not found
    @ExceptionHandler(EntityNotFoundException::class, NoSuchElementException::class)
    fun handleNotFound(ex: RuntimeException, req: WebRequest): ResponseEntity<ErrorResponse> =
        build(HttpStatus.NOT_FOUND, req, "not_found", ex.message ?: "resource not found")

    // 500 - Fallback
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception, req: WebRequest): ResponseEntity<ErrorResponse> =
        build(HttpStatus.INTERNAL_SERVER_ERROR, req, "internal_error", "Unexpected error")
}