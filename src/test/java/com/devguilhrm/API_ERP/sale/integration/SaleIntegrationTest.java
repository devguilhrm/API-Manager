package com.devguilhrm.API_ERP.sale.integration;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.repository.UserRepository;
import com.devguilhrm.API_ERP.clients.entity.Client;
import com.devguilhrm.API_ERP.clients.repository.ClientRepository;
import com.devguilhrm.API_ERP.common.enums.PaymentMethod;
import com.devguilhrm.API_ERP.product.entity.Product;
import com.devguilhrm.API_ERP.product.repository.ProductRepository;
import com.devguilhrm.API_ERP.sale.dto.CancelSaleRequest;
import com.devguilhrm.API_ERP.sale.dto.CreateSaleItemRequest;
import com.devguilhrm.API_ERP.sale.dto.CreateSaleRequest;
import com.devguilhrm.API_ERP.sale.enums.SaleStatus;
import com.devguilhrm.API_ERP.sale.service.SaleService;
import com.devguilhrm.API_ERP.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SaleIntegrationTest {

	@Autowired
	private SaleService saleService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private ProductRepository productRepository;

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldRunCompleteSaleFlowEndToEnd() {
		User seller = userRepository.save(User.builder()
				.name("Seller " + UUID.randomUUID())
				.email("seller-" + UUID.randomUUID() + "@crm.com")
				.password("hash")
				.role(Role.SELLER)
				.enabled(true)
				.build());
		User manager = userRepository.save(User.builder()
				.name("Manager " + UUID.randomUUID())
				.email("manager-" + UUID.randomUUID() + "@crm.com")
				.password("hash")
				.role(Role.MANAGER)
				.enabled(true)
				.build());
		Client client = clientRepository.save(Client.builder()
				.name("Client")
				.email("client-" + UUID.randomUUID() + "@crm.com")
				.phone("999")
				.seller(seller)
				.build());
		Product product = productRepository.save(Product.builder()
				.name("Product")
				.description("Desc")
				.price(new BigDecimal("100.00"))
				.stockQuantity(10)
				.active(true)
				.build());

		authenticate(seller);
		var pending = saleService.create(new CreateSaleRequest(
				client.getId(),
				PaymentMethod.PIX,
				BigDecimal.ZERO,
				List.of(new CreateSaleItemRequest(product.getId(), 3))
		));

		assertThat(pending.status()).isEqualTo(SaleStatus.PENDING);
		assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(10);

		authenticate(manager);
		var completed = saleService.complete(pending.id());

		assertThat(completed.status()).isEqualTo(SaleStatus.COMPLETED);
		assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(7);

		var cancelled = saleService.cancel(pending.id(), new CancelSaleRequest("Cliente solicitou cancelamento"));

		assertThat(cancelled.status()).isEqualTo(SaleStatus.CANCELLED);
		assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(10);
	}

	private void authenticate(User user) {
		UserPrincipal principal = new UserPrincipal(user);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
				principal,
				null,
				principal.getAuthorities()
		));
	}
}
