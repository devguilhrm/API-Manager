package com.devguilhrm.API_ERP.sale.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SaleCancelledEvent(
		UUID saleId,
		UUID sellerId,
		BigDecimal totalAmount,
		String reason,
		Instant occurredAt
) {
}
