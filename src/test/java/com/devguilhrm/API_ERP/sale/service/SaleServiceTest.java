package com.devguilhrm.API_ERP.sale.service;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.service.AuthService;
import com.devguilhrm.API_ERP.clients.entity.Client;
import com.devguilhrm.API_ERP.clients.repository.ClientRepository;
import com.devguilhrm.API_ERP.common.enums.PaymentMethod;
import com.devguilhrm.API_ERP.exception.BusinessException;
import com.devguilhrm.API_ERP.exception.InsufficientStockException;
import com.devguilhrm.API_ERP.product.entity.Product;
import com.devguilhrm.API_ERP.product.repository.ProductRepository;
import com.devguilhrm.API_ERP.sale.dto.CancelSaleRequest;
import com.devguilhrm.API_ERP.sale.dto.CreateSaleItemRequest;
import com.devguilhrm.API_ERP.sale.dto.CreateSaleRequest;
import com.devguilhrm.API_ERP.sale.dto.SaleDTO;
import com.devguilhrm.API_ERP.sale.entity.Sale;
import com.devguilhrm.API_ERP.sale.entity.SaleItem;
import com.devguilhrm.API_ERP.sale.enums.SaleStatus;
import com.devguilhrm.API_ERP.sale.mapper.SaleMapper;
import com.devguilhrm.API_ERP.sale.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

	@Mock
	private SaleRepository saleRepository;

	@Mock
	private ClientRepository clientRepository;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private SaleMapper saleMapper;

	@Mock
	private AuthService authService;

	@InjectMocks
	private SaleServiceImpl saleService;

	private User seller;
	private User manager;
	private Client client;
	private Product product;

	@BeforeEach
	void setUp() {
		seller = User.builder().name("Seller").email("seller@crm.com").role(Role.SELLER).enabled(true).build();
		seller.setId(UUID.randomUUID());
		manager = User.builder().name("Manager").email("manager@crm.com").role(Role.MANAGER).enabled(true).build();
		manager.setId(UUID.randomUUID());
		client = Client.builder().name("Client").email("client@crm.com").phone("999").seller(seller).build();
		client.setId(UUID.randomUUID());
		product = Product.builder().name("Product").price(new BigDecimal("10.00")).stockQuantity(5).active(true).build();
		product.setId(UUID.randomUUID());
	}

	@Test
	void createShouldKeepStockAndCreatePendingSale() {
		when(authService.getAuthenticatedUser()).thenReturn(seller);
		when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
		when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
		when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(saleMapper.toDto(any(Sale.class))).thenReturn(new SaleDTO(null, null, null, null, null, SaleStatus.PENDING, null, null, null, null, List.of(), null, null));

		saleService.create(new CreateSaleRequest(client.getId(), PaymentMethod.PIX, BigDecimal.ZERO, List.of(new CreateSaleItemRequest(product.getId(), 2))));

		ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
		verify(saleRepository).save(captor.capture());
		assertThat(captor.getValue().getStatus()).isEqualTo(SaleStatus.PENDING);
		assertThat(product.getStockQuantity()).isEqualTo(5);
	}

	@Test
	void completeShouldDebitStockWithLockedProduct() {
		Sale sale = pendingSale(2);
		when(authService.getAuthenticatedUser()).thenReturn(manager);
		when(saleRepository.findById(sale.getId())).thenReturn(Optional.of(sale));
		when(productRepository.findByIdForUpdate(product.getId())).thenReturn(Optional.of(product));
		when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(saleMapper.toDto(any(Sale.class))).thenReturn(new SaleDTO(sale.getId(), null, null, null, null, SaleStatus.COMPLETED, null, null, null, null, List.of(), null, null));

		saleService.complete(sale.getId());

		assertThat(product.getStockQuantity()).isEqualTo(3);
		assertThat(sale.getStatus()).isEqualTo(SaleStatus.COMPLETED);
	}

	@Test
	void cancelCompletedSaleShouldReturnStock() {
		Sale sale = pendingSale(2);
		sale.setStatus(SaleStatus.COMPLETED);
		product.setStockQuantity(3);
		when(authService.getAuthenticatedUser()).thenReturn(manager);
		when(saleRepository.findById(sale.getId())).thenReturn(Optional.of(sale));
		when(productRepository.findByIdForUpdate(product.getId())).thenReturn(Optional.of(product));
		when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(saleMapper.toDto(any(Sale.class))).thenReturn(new SaleDTO(sale.getId(), null, null, null, null, SaleStatus.CANCELLED, null, null, null, "motivo", List.of(), null, null));

		saleService.cancel(sale.getId(), new CancelSaleRequest("motivo"));

		assertThat(product.getStockQuantity()).isEqualTo(5);
		assertThat(sale.getStatus()).isEqualTo(SaleStatus.CANCELLED);
	}

	@Test
	void completeShouldPreventOverselling() {
		Sale sale = pendingSale(8);
		when(authService.getAuthenticatedUser()).thenReturn(manager);
		when(saleRepository.findById(sale.getId())).thenReturn(Optional.of(sale));
		when(productRepository.findByIdForUpdate(product.getId())).thenReturn(Optional.of(product));

		assertThatThrownBy(() -> saleService.complete(sale.getId()))
				.isInstanceOf(InsufficientStockException.class);

		verify(saleRepository, never()).save(any(Sale.class));
	}

	@Test
	void cancelledSaleCannotTransition() {
		Sale sale = pendingSale(1);
		sale.setStatus(SaleStatus.CANCELLED);
		when(authService.getAuthenticatedUser()).thenReturn(manager);
		when(saleRepository.findById(sale.getId())).thenReturn(Optional.of(sale));

		assertThatThrownBy(() -> saleService.complete(sale.getId()))
				.isInstanceOf(BusinessException.class);
	}

	private Sale pendingSale(int quantity) {
		Sale sale = Sale.builder()
				.client(client)
				.seller(seller)
				.status(SaleStatus.PENDING)
				.paymentMethod(PaymentMethod.PIX)
				.discount(BigDecimal.ZERO)
				.totalAmount(BigDecimal.valueOf(quantity * 10L))
				.build();
		sale.setId(UUID.randomUUID());
		SaleItem item = SaleItem.builder()
				.product(product)
				.productName(product.getName())
				.quantity(quantity)
				.unitPrice(product.getPrice())
				.totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
				.build();
		sale.addItem(item);
		return sale;
	}
}
