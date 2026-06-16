package com.devguilhrm.API_ERP.auth.dto;

import com.devguilhrm.API_ERP.auth.enums.Role;

import java.util.UUID;

public record UserDTO(
		UUID id,
		String name,
		String email,
		Role role,
		boolean enabled
) {
}
