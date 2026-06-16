package com.devguilhrm.API_ERP.auth.dto;

import com.devguilhrm.API_ERP.auth.enums.Role;

import java.util.UUID;

public record AuthResponse(
		String accessToken,
		String refreshToken,
		UUID userId,
		String name,
		String email,
		Role role
) {
}
