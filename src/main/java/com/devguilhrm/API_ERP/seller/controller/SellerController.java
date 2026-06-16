package com.devguilhrm.API_ERP.seller.controller;

import com.devguilhrm.API_ERP.auth.dto.CreateUserRequest;
import com.devguilhrm.API_ERP.auth.dto.UserDTO;
import com.devguilhrm.API_ERP.common.response.ApiResponse;
import com.devguilhrm.API_ERP.common.response.PageResponse;
import com.devguilhrm.API_ERP.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Vendedores", description = "Gestao de vendedores")
@RestController
@RequestMapping("/sellers")
@PreAuthorize("hasRole('MANAGER')")
public class SellerController {

	private final SellerService sellerService;

	public SellerController(SellerService sellerService) {
		this.sellerService = sellerService;
	}

	@Operation(summary = "Cria vendedor", description = "Cria novo usuario vendedor")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Vendedor criado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado")
	@PostMapping
	public ResponseEntity<ApiResponse<UserDTO>> create(@Valid @RequestBody CreateUserRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Vendedor criado com sucesso", sellerService.createSeller(request)));
	}

	@Operation(summary = "Lista vendedores", description = "Retorna vendedores paginados")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vendedores listados")
	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> list(Pageable pageable) {
		return ResponseEntity.ok(ApiResponse.success("Vendedores listados com sucesso", PageResponse.from(sellerService.list(pageable))));
	}
}
