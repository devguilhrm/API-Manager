package com.devguilhrm.API_ERP.sale.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateSaleItemRequest(
		@NotNull(message = "Produto e obrigatorio")
		UUID productId,

		@NotNull(message = "Quantidade e obrigatoria")
		@Min(value = 1, message = "Quantidade minima e 1")
		Integer quantity
) {
}
