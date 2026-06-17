package com.devguilhrm.API_ERP.sale.event;

import java.time.Instant;
import java.util.UUID;

public record StockUpdatedEvent(
		UUID saleId,
		UUID productId,
		int deltaQuantity,
		int stockQuantity,
		String operation,
		Instant occurredAt
) {
}
