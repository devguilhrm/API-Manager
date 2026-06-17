package com.devguilhrm.API_ERP.product.controller;

import com.devguilhrm.API_ERP.exception.GlobalExceptionHandler;
import com.devguilhrm.API_ERP.product.dto.ProductDTO;
import com.devguilhrm.API_ERP.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

	@Mock
	private ProductService productService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(productService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator())
				.build();
	}

	@Test
	void createShouldReturnCreatedProduct() throws Exception {
		UUID productId = UUID.randomUUID();
		when(productService.create(org.mockito.ArgumentMatchers.any()))
				.thenReturn(new ProductDTO(productId, "Notebook Pro", "Commercial notebook",
						new BigDecimal("5500.00"), 12, true, null, null));

		mockMvc.perform(post("/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Notebook Pro",
								  "description": "Commercial notebook",
								  "price": 5500.00,
								  "stockQuantity": 12
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Produto criado com sucesso"))
				.andExpect(jsonPath("$.data.id").value(productId.toString()))
				.andExpect(jsonPath("$.data.name").value("Notebook Pro"))
				.andExpect(jsonPath("$.data.active").value(true));
	}

	@Test
	void createShouldValidateRequestBody() throws Exception {
		mockMvc.perform(post("/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "",
								  "price": -1,
								  "stockQuantity": -1
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Dados invalidos"))
				.andExpect(jsonPath("$.errors[0]", containsString(":")));
	}

	private LocalValidatorFactoryBean validator() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		return validator;
	}
}
