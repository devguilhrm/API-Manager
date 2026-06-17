package com.devguilhrm.API_ERP.sale.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SaleCreatedEvent(
		UUID saleId,
		UUID sellerId,
		UUID clientId,
		BigDecimal totalAmount,
		Instant occurredAt
) {
}
