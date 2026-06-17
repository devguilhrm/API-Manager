package com.devguilhrm.API_ERP.sale.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SaleCompletedEvent(
		UUID saleId,
		UUID sellerId,
		BigDecimal totalAmount,
		Instant occurredAt
) {
}
