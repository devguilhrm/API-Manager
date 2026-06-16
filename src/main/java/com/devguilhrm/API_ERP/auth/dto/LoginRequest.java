package com.devguilhrm.API_ERP.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "Email e obrigatorio")
		@Email(message = "Informe um email valido")
		String email,

		@NotBlank(message = "Senha e obrigatoria")
		String password
) {
}
