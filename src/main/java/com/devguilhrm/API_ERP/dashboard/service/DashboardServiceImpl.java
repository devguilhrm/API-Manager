package com.devguilhrm.API_ERP.dashboard.service;

import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.repository.UserRepository;
import com.devguilhrm.API_ERP.clients.repository.ClientRepository;
import com.devguilhrm.API_ERP.dashboard.dto.DashboardDTO;
import com.devguilhrm.API_ERP.dashboard.dto.RevenueBySellerDTO;
import com.devguilhrm.API_ERP.product.repository.ProductRepository;
import com.devguilhrm.API_ERP.sale.enums.SaleStatus;
import com.devguilhrm.API_ERP.sale.repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

	private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

	private final ClientRepository clientRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final SaleRepository saleRepository;

	public DashboardServiceImpl(
			ClientRepository clientRepository,
			ProductRepository productRepository,
			UserRepository userRepository,
			SaleRepository saleRepository
	) {
		this.clientRepository = clientRepository;
		this.productRepository = productRepository;
		this.userRepository = userRepository;
		this.saleRepository = saleRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public DashboardDTO getGlobalDashboard(LocalDate from, LocalDate to) {
		log.info("Gerando dashboard global");
		LocalDateTime fromDate = from == null ? null : from.atStartOfDay();
		LocalDateTime toDate = to == null ? null : to.plusDays(1).atStartOfDay();
		List<RevenueBySellerDTO> revenueBySeller = saleRepository.sumRevenueBySellerAndPeriod(SaleStatus.COMPLETED, fromDate, toDate).stream()
				.map(row -> new RevenueBySellerDTO((String) row[0], (BigDecimal) row[1]))
				.toList();
		return new DashboardDTO(
				clientRepository.count(),
				productRepository.count(),
				userRepository.countByRole(Role.SELLER),
				saleRepository.countByPeriod(fromDate, toDate),
				saleRepository.sumTotalByStatusAndPeriod(SaleStatus.COMPLETED, fromDate, toDate),
				revenueBySeller
		);
	}
}
