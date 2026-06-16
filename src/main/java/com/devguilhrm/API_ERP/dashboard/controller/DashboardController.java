package com.devguilhrm.API_ERP.dashboard.controller;

import com.devguilhrm.API_ERP.common.response.ApiResponse;
import com.devguilhrm.API_ERP.dashboard.dto.DashboardDTO;
import com.devguilhrm.API_ERP.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "Indicadores gerenciais")
@RestController
@RequestMapping("/dashboard")
@PreAuthorize("hasRole('MANAGER')")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@Operation(summary = "Dashboard global", description = "Retorna indicadores globais para gerentes")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard carregado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado")
	@GetMapping
	public ResponseEntity<ApiResponse<DashboardDTO>> dashboard() {
		return ResponseEntity.ok(ApiResponse.success("Dashboard carregado com sucesso", dashboardService.getGlobalDashboard()));
	}
}
