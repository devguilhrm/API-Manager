package com.devguilhrm.API_ERP.clients.controller;

import com.devguilhrm.API_ERP.clients.dto.ClientDTO;
import com.devguilhrm.API_ERP.clients.dto.CreateClientRequest;
import com.devguilhrm.API_ERP.clients.dto.ReassignClientRequest;
import com.devguilhrm.API_ERP.clients.dto.UpdateClientRequest;
import com.devguilhrm.API_ERP.clients.service.ClientService;
import com.devguilhrm.API_ERP.common.response.ApiResponse;
import com.devguilhrm.API_ERP.common.response.PageResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Clientes", description = "Gestao de clientes")
@RestController
@RequestMapping("/clients")
public class ClientController {

	private final ClientService clientService;

	public ClientController(ClientService clientService) {
		this.clientService = clientService;
	}

	@Operation(summary = "Lista clientes", description = "Managers veem todos; sellers veem apenas seus clientes")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Clientes listados")
	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<ClientDTO>>> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) UUID sellerId,
			Pageable pageable
	) {
		return ResponseEntity.ok(ApiResponse.success("Clientes listados com sucesso",
				PageResponse.from(clientService.list(search, sellerId, pageable))));
	}

	@Operation(summary = "Busca cliente", description = "Busca cliente por ID respeitando isolamento por vendedor")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cliente encontrado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente nao encontrado")
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ClientDTO>> getById(@PathVariable UUID id) {
		return ResponseEntity.ok(ApiResponse.success("Cliente encontrado", clientService.getById(id)));
	}

	@Operation(summary = "Cria cliente", description = "Cria cliente vinculado automaticamente ao vendedor autenticado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Cliente criado")
	@PreAuthorize("hasRole('SELLER')")
	@PostMapping
	public ResponseEntity<ApiResponse<ClientDTO>> create(@Valid @RequestBody CreateClientRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Cliente criado com sucesso", clientService.create(request)));
	}

	@Operation(summary = "Atualiza cliente", description = "Atualiza cliente respeitando isolamento por vendedor")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cliente atualizado")
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ClientDTO>> update(@PathVariable UUID id, @Valid @RequestBody UpdateClientRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Cliente atualizado com sucesso", clientService.update(id, request)));
	}

	@Operation(summary = "Reatribui cliente", description = "Reatribui cliente para outro vendedor")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cliente reatribuido")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado")
	@PreAuthorize("hasRole('MANAGER')")
	@PutMapping("/{id}/reassign")
	public ResponseEntity<ApiResponse<ClientDTO>> reassign(@PathVariable UUID id, @Valid @RequestBody ReassignClientRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Cliente reatribuido com sucesso", clientService.reassign(id, request)));
	}
}
