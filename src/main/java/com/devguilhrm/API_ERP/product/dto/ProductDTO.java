package com.devguilhrm.API_ERP.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductDTO(
		UUID id,
		String name,
		String description,
		BigDecimal price,
		Integer stockQuantity,
		boolean active,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) implements Serializable {
}
