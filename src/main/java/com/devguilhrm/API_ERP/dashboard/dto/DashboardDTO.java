package com.devguilhrm.API_ERP.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDTO(
		long totalClients,
		long totalProducts,
		long totalSellers,
		long totalSales,
		BigDecimal completedRevenue,
		List<RevenueBySellerDTO> revenueBySeller
) {
}
