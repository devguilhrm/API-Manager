package com.devguilhrm.API_ERP.dashboard.dto;

import java.math.BigDecimal;

public record RevenueBySellerDTO(
		String sellerName,
		BigDecimal revenue
) {
}
