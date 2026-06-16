package com.devguilhrm.API_ERP.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
		@NotBlank(message = "Refresh token e obrigatorio")
		String refreshToken
) {
}
