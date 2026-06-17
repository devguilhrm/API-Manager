package com.devguilhrm.API_ERP.dashboard.service;

import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.repository.UserRepository;
import com.devguilhrm.API_ERP.clients.repository.ClientRepository;
import com.devguilhrm.API_ERP.product.repository.ProductRepository;
import com.devguilhrm.API_ERP.sale.enums.SaleStatus;
import com.devguilhrm.API_ERP.sale.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

	@Mock
	private ClientRepository clientRepository;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private SaleRepository saleRepository;

	@InjectMocks
	private DashboardServiceImpl dashboardService;

	@Test
	void getGlobalDashboardShouldAggregateRepositoryValuesForPeriod() {
		LocalDate from = LocalDate.of(2026, 1, 10);
		LocalDate to = LocalDate.of(2026, 1, 12);
		LocalDateTime fromDate = LocalDateTime.of(2026, 1, 10, 0, 0);
		LocalDateTime toDate = LocalDateTime.of(2026, 1, 13, 0, 0);

		when(clientRepository.count()).thenReturn(4L);
		when(productRepository.count()).thenReturn(8L);
		when(userRepository.countByRole(Role.SELLER)).thenReturn(2L);
		when(saleRepository.countByPeriod(fromDate, toDate)).thenReturn(6L);
		when(saleRepository.sumTotalByStatusAndPeriod(SaleStatus.COMPLETED, fromDate, toDate))
				.thenReturn(new BigDecimal("150.50"));
		when(saleRepository.sumRevenueBySellerAndPeriod(SaleStatus.COMPLETED, fromDate, toDate))
				.thenReturn(Collections.singletonList(new Object[] {"Seller", new BigDecimal("150.50")}));

		var dashboard = dashboardService.getGlobalDashboard(from, to);

		assertThat(dashboard.totalClients()).isEqualTo(4);
		assertThat(dashboard.totalProducts()).isEqualTo(8);
		assertThat(dashboard.totalSellers()).isEqualTo(2);
		assertThat(dashboard.totalSales()).isEqualTo(6);
		assertThat(dashboard.completedRevenue()).isEqualByComparingTo("150.50");
		assertThat(dashboard.revenueBySeller()).hasSize(1);
		assertThat(dashboard.revenueBySeller().get(0).sellerName()).isEqualTo("Seller");
		assertThat(dashboard.revenueBySeller().get(0).revenue()).isEqualByComparingTo("150.50");
	}

	@Test
	void getGlobalDashboardShouldAcceptNullPeriod() {
		when(clientRepository.count()).thenReturn(1L);
		when(productRepository.count()).thenReturn(2L);
		when(userRepository.countByRole(Role.SELLER)).thenReturn(3L);
		when(saleRepository.countByPeriod(null, null)).thenReturn(4L);
		when(saleRepository.sumTotalByStatusAndPeriod(SaleStatus.COMPLETED, null, null))
				.thenReturn(BigDecimal.TEN);
		when(saleRepository.sumRevenueBySellerAndPeriod(SaleStatus.COMPLETED, null, null))
				.thenReturn(List.of());

		var dashboard = dashboardService.getGlobalDashboard(null, null);

		assertThat(dashboard.totalClients()).isEqualTo(1);
		assertThat(dashboard.totalProducts()).isEqualTo(2);
		assertThat(dashboard.totalSellers()).isEqualTo(3);
		assertThat(dashboard.totalSales()).isEqualTo(4);
		assertThat(dashboard.completedRevenue()).isEqualByComparingTo("10");
		assertThat(dashboard.revenueBySeller()).isEmpty();
	}
}
