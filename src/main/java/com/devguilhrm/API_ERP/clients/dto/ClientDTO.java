package com.devguilhrm.API_ERP.clients.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClientDTO(
		UUID id,
		String name,
		String email,
		String phone,
		String address,
		UUID sellerId,
		String sellerName,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) implements Serializable {
}
