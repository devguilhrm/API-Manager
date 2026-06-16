package com.devguilhrm.API_ERP.clients.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReassignClientRequest(
		@NotNull(message = "Novo vendedor e obrigatorio")
		UUID sellerId
) {
}
