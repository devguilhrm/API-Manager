package com.devguilhrm.API_ERP.manager.controller;

import com.devguilhrm.API_ERP.auth.dto.UserDTO;
import com.devguilhrm.API_ERP.common.response.ApiResponse;
import com.devguilhrm.API_ERP.manager.service.ManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Gerentes", description = "Operacoes especificas de gerentes")
@RestController
@RequestMapping("/managers")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerController {

	private final ManagerService managerService;

	public ManagerController(ManagerService managerService) {
		this.managerService = managerService;
	}

	@Operation(summary = "Gerente autenticado", description = "Retorna dados do gerente autenticado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Gerente encontrado")
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserDTO>> me() {
		return ResponseEntity.ok(ApiResponse.success("Gerente autenticado", managerService.currentManager()));
	}
}
