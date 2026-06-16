package com.devguilhrm.API_ERP.sale.dto;

import com.devguilhrm.API_ERP.common.enums.PaymentMethod;
import com.devguilhrm.API_ERP.sale.enums.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SaleDTO(
		UUID id,
		UUID clientId,
		String clientName,
		UUID sellerId,
		String sellerName,
		SaleStatus status,
		PaymentMethod paymentMethod,
		BigDecimal discount,
		BigDecimal totalAmount,
		String cancelReason,
		List<SaleItemDTO> items,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
