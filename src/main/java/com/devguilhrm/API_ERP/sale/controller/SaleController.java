package com.devguilhrm.API_ERP.sale.controller;

import com.devguilhrm.API_ERP.common.response.ApiResponse;
import com.devguilhrm.API_ERP.common.response.PageResponse;
import com.devguilhrm.API_ERP.sale.dto.CancelSaleRequest;
import com.devguilhrm.API_ERP.sale.dto.CreateSaleRequest;
import com.devguilhrm.API_ERP.sale.dto.SaleDTO;
import com.devguilhrm.API_ERP.sale.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Vendas", description = "Fluxo de vendas e itens")
@RestController
@RequestMapping("/sales")
public class SaleController {

	private final SaleService saleService;

	public SaleController(SaleService saleService) {
		this.saleService = saleService;
	}

	@Operation(summary = "Lista vendas", description = "Managers veem todas; sellers veem apenas suas vendas")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vendas listadas")
	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<SaleDTO>>> list(Pageable pageable) {
		return ResponseEntity.ok(ApiResponse.success("Vendas listadas com sucesso", PageResponse.from(saleService.list(pageable))));
	}

	@Operation(summary = "Busca venda", description = "Busca venda por ID respeitando isolamento por vendedor")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Venda encontrada")
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<SaleDTO>> getById(@PathVariable UUID id) {
		return ResponseEntity.ok(ApiResponse.success("Venda encontrada", saleService.getById(id)));
	}

	@Operation(summary = "Cria venda", description = "Cria venda pendente, validando estoque sem debitar")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Venda criada")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Estoque insuficiente")
	@PreAuthorize("hasRole('SELLER')")
	@PostMapping
	public ResponseEntity<ApiResponse<SaleDTO>> create(@Valid @RequestBody CreateSaleRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Venda criada com sucesso", saleService.create(request)));
	}

	@Operation(summary = "Finaliza venda", description = "Finaliza venda pendente com lock pessimista no estoque")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Venda finalizada")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Estoque insuficiente")
	@PreAuthorize("hasRole('MANAGER')")
	@PutMapping("/{id}/complete")
	public ResponseEntity<ApiResponse<SaleDTO>> complete(@PathVariable UUID id) {
		return ResponseEntity.ok(ApiResponse.success("Venda finalizada com sucesso", saleService.complete(id)));
	}

	@Operation(summary = "Cancela venda", description = "Cancela venda com motivo obrigatorio e estorna estoque quando concluida")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Venda cancelada")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Transicao invalida")
	@PreAuthorize("hasRole('MANAGER')")
	@PutMapping("/{id}/cancel")
	public ResponseEntity<ApiResponse<SaleDTO>> cancel(@PathVariable UUID id, @Valid @RequestBody CancelSaleRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Venda cancelada com sucesso", saleService.cancel(id, request)));
	}
}
