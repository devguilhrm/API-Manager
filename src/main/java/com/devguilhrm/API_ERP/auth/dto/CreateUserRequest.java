package com.devguilhrm.API_ERP.auth.dto;

import com.devguilhrm.API_ERP.auth.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
		@NotBlank(message = "Nome e obrigatorio")
		String name,

		@NotBlank(message = "Email e obrigatorio")
		@Email(message = "Informe um email valido")
		String email,

		@NotBlank(message = "Senha e obrigatoria")
		String password,

		@NotNull(message = "Perfil e obrigatorio")
		Role role
) {
}
