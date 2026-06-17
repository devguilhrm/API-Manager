package com.devguilhrm.API_ERP.product.controller;

import com.devguilhrm.API_ERP.common.response.ApiResponse;
import com.devguilhrm.API_ERP.common.response.PageResponse;
import com.devguilhrm.API_ERP.product.dto.CreateProductRequest;
import com.devguilhrm.API_ERP.product.dto.ProductDTO;
import com.devguilhrm.API_ERP.product.dto.UpdateProductRequest;
import com.devguilhrm.API_ERP.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Produtos", description = "Catalogo de produtos e estoque")
@RestController
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@Operation(summary = "Lista produtos", description = "Retorna produtos paginados")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Produtos listados")
	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) Boolean active,
			@RequestParam(required = false) Integer lowStockThreshold,
			Pageable pageable
	) {
		return ResponseEntity.ok(ApiResponse.success("Produtos listados com sucesso",
				PageResponse.from(productService.list(search, active, lowStockThreshold, pageable))));
	}

	@Operation(summary = "Busca produto", description = "Retorna produto por ID")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Produto encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Produto nao encontrado")
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductDTO>> getById(@PathVariable UUID id) {
		return ResponseEntity.ok(ApiResponse.success("Produto encontrado", productService.getById(id)));
	}

	@Operation(summary = "Cria produto", description = "Cria produto no catalogo")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Produto criado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado")
	@PreAuthorize("hasRole('MANAGER')")
	@PostMapping
	public ResponseEntity<ApiResponse<ProductDTO>> create(@Valid @RequestBody CreateProductRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Produto criado com sucesso", productService.create(request)));
	}

	@Operation(summary = "Atualiza produto", description = "Atualiza dados e estoque do produto")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Produto atualizado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Produto nao encontrado")
	@PreAuthorize("hasRole('MANAGER')")
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductDTO>> update(@PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Produto atualizado com sucesso", productService.update(id, request)));
	}

	@Operation(summary = "Remove produto", description = "Desativa produto no catalogo")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Produto removido")
	@PreAuthorize("hasRole('MANAGER')")
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
		productService.delete(id);
		return ResponseEntity.ok(ApiResponse.success("Produto removido com sucesso", null));
	}
}
