package com.devguilhrm.API_ERP.sale.service;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.service.AuthService;
import com.devguilhrm.API_ERP.clients.entity.Client;
import com.devguilhrm.API_ERP.clients.repository.ClientRepository;
import com.devguilhrm.API_ERP.exception.BusinessException;
import com.devguilhrm.API_ERP.exception.InsufficientStockException;
import com.devguilhrm.API_ERP.exception.ResourceNotFoundException;
import com.devguilhrm.API_ERP.exception.UnauthorizedOperationException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class SaleServiceImpl implements SaleService {

	private static final Logger log = LoggerFactory.getLogger(SaleServiceImpl.class);

	private final SaleRepository saleRepository;
	private final ClientRepository clientRepository;
	private final ProductRepository productRepository;
	private final SaleMapper saleMapper;
	private final AuthService authService;

	public SaleServiceImpl(
			SaleRepository saleRepository,
			ClientRepository clientRepository,
			ProductRepository productRepository,
			SaleMapper saleMapper,
			AuthService authService
	) {
		this.saleRepository = saleRepository;
		this.clientRepository = clientRepository;
		this.productRepository = productRepository;
		this.saleMapper = saleMapper;
		this.authService = authService;
	}

	@Override
	@Transactional
	public SaleDTO create(CreateSaleRequest request) {
		User seller = authService.getAuthenticatedUser();
		if (seller.getRole() != Role.SELLER) {
			throw new UnauthorizedOperationException("Apenas vendedores podem criar vendas");
		}
		Client client = clientRepository.findById(request.clientId())
				.orElseThrow(() -> new ResourceNotFoundException("Cliente", request.clientId()));
		if (!client.getSeller().getId().equals(seller.getId())) {
			log.warn("Vendedor {} tentou vender para cliente {}", seller.getEmail(), client.getId());
			throw new UnauthorizedOperationException("Cliente pertence a outro vendedor");
		}

		Sale sale = Sale.builder()
				.client(client)
				.seller(seller)
				.status(SaleStatus.PENDING)
				.paymentMethod(request.paymentMethod())
				.discount(request.discount() == null ? BigDecimal.ZERO : request.discount())
				.totalAmount(BigDecimal.ZERO)
				.build();

		BigDecimal subtotal = BigDecimal.ZERO;
		for (CreateSaleItemRequest itemRequest : request.items()) {
			Product product = productRepository.findById(itemRequest.productId())
					.orElseThrow(() -> new ResourceNotFoundException("Produto", itemRequest.productId()));
			validateStock(product, itemRequest.quantity());
			BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
			subtotal = subtotal.add(itemTotal);
			sale.addItem(SaleItem.builder()
					.product(product)
					.productName(product.getName())
					.quantity(itemRequest.quantity())
					.unitPrice(product.getPrice())
					.totalPrice(itemTotal)
					.build());
		}
		BigDecimal total = subtotal.subtract(sale.getDiscount());
		if (total.compareTo(BigDecimal.ZERO) < 0) {
			throw new BusinessException("Desconto nao pode ser maior que subtotal da venda");
		}
		sale.setTotalAmount(total);
		log.info("Criando venda pendente para cliente {}", client.getId());
		return saleMapper.toDto(saleRepository.save(sale));
	}

	@Override
	@Transactional
	public SaleDTO complete(UUID id) {
		ensureManager();
		Sale sale = saleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Venda", id));
		ensureCanTransition(sale);
		if (sale.getStatus() == SaleStatus.COMPLETED) {
			return saleMapper.toDto(sale);
		}
		for (SaleItem item : sale.getItems()) {
			Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
					.orElseThrow(() -> new ResourceNotFoundException("Produto", item.getProduct().getId()));
			validateStock(product, item.getQuantity());
			product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
			productRepository.save(product);
		}
		sale.setStatus(SaleStatus.COMPLETED);
		log.info("Venda {} finalizada", id);
		return saleMapper.toDto(saleRepository.save(sale));
	}

	@Override
	@Transactional
	public SaleDTO cancel(UUID id, CancelSaleRequest request) {
		ensureManager();
		Sale sale = saleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Venda", id));
		ensureCanTransition(sale);
		if (sale.getStatus() == SaleStatus.COMPLETED) {
			for (SaleItem item : sale.getItems()) {
				Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
						.orElseThrow(() -> new ResourceNotFoundException("Produto", item.getProduct().getId()));
				product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
				productRepository.save(product);
			}
		}
		sale.setStatus(SaleStatus.CANCELLED);
		sale.setCancelReason(request.reason());
		log.info("Venda {} cancelada", id);
		return saleMapper.toDto(saleRepository.save(sale));
	}

	@Override
	@Transactional(readOnly = true)
	public SaleDTO getById(UUID id) {
		return saleMapper.toDto(findVisibleSale(id));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<SaleDTO> list(Pageable pageable) {
		User user = authService.getAuthenticatedUser();
		if (user.getRole() == Role.SELLER) {
			return saleRepository.findAllBySellerId(user.getId(), pageable).map(saleMapper::toDto);
		}
		return saleRepository.findAll(pageable).map(saleMapper::toDto);
	}

	private Sale findVisibleSale(UUID id) {
		Sale sale = saleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Venda", id));
		User user = authService.getAuthenticatedUser();
		if (user.getRole() == Role.SELLER && !sale.getSeller().getId().equals(user.getId())) {
			throw new UnauthorizedOperationException("Venda pertence a outro vendedor");
		}
		return sale;
	}

	private void ensureCanTransition(Sale sale) {
		if (sale.getStatus() == SaleStatus.CANCELLED) {
			log.warn("Tentativa de transicao a partir de venda cancelada {}", sale.getId());
			throw new BusinessException("Venda cancelada nao pode mudar de estado");
		}
	}

	private void ensureManager() {
		User user = authService.getAuthenticatedUser();
		if (user.getRole() != Role.MANAGER) {
			throw new UnauthorizedOperationException("Apenas gerentes podem finalizar ou cancelar vendas");
		}
	}

	private void validateStock(Product product, int requested) {
		if (!product.isActive() || product.getStockQuantity() < requested) {
			throw new InsufficientStockException(product.getId(), requested, product.getStockQuantity());
		}
	}
}
