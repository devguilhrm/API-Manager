package com.devguilhrm.API_ERP.clients.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateClientRequest(
		@NotBlank(message = "Nome do cliente e obrigatorio")
		String name,

		@NotBlank(message = "Email do cliente e obrigatorio")
		@Email(message = "Informe um email valido")
		String email,

		@NotBlank(message = "Telefone do cliente e obrigatorio")
		String phone,

		String address
) {
}
