package com.devguilhrm.API_ERP.product.service;

import com.devguilhrm.API_ERP.product.dto.CreateProductRequest;
import com.devguilhrm.API_ERP.product.dto.ProductDTO;
import com.devguilhrm.API_ERP.product.entity.Product;
import com.devguilhrm.API_ERP.product.mapper.ProductMapper;
import com.devguilhrm.API_ERP.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private ProductMapper productMapper;

	@InjectMocks
	private ProductServiceImpl productService;

	@Test
	void createShouldPersistProduct() {
		var request = new CreateProductRequest("Produto", "Desc", new BigDecimal("10.00"), 5);
		Product product = Product.builder().name("Produto").description("Desc").price(new BigDecimal("10.00")).stockQuantity(5).active(true).build();
		product.setId(UUID.randomUUID());
		ProductDTO dto = new ProductDTO(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getStockQuantity(), true, null, null);

		when(productMapper.toEntity(request)).thenReturn(product);
		when(productRepository.save(product)).thenReturn(product);
		when(productMapper.toDto(product)).thenReturn(dto);

		assertThat(productService.create(request)).isEqualTo(dto);
	}
}
