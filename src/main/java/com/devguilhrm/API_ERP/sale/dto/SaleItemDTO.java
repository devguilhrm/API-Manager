package com.devguilhrm.API_ERP.sale.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleItemDTO(
		UUID id,
		UUID productId,
		String productName,
		Integer quantity,
		BigDecimal unitPrice,
		BigDecimal totalPrice
) {
}
