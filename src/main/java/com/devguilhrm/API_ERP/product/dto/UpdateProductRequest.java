package com.devguilhrm.API_ERP.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateProductRequest(
		@NotBlank(message = "Nome do produto e obrigatorio")
		String name,

		String description,

		@NotNull(message = "Preco e obrigatorio")
		@DecimalMin(value = "0.0", message = "Preco deve ser maior ou igual a zero")
		BigDecimal price,

		@NotNull(message = "Estoque e obrigatorio")
		@Min(value = 0, message = "Estoque nao pode ser negativo")
		Integer stockQuantity,

		boolean active
) {
}
