package com.dowglasmaia.demouserserviceapi.domain.exception

class EmailAlreadyUsedException(email: String) : RuntimeException("email already used: $email")

class CpfAlreadyUsedException(cpf: String) : RuntimeException("CPF already used: $cpf")