package com.devguilhrm.API_ERP.auth.controller;

import com.devguilhrm.API_ERP.auth.dto.AuthResponse;
import com.devguilhrm.API_ERP.auth.dto.LoginRequest;
import com.devguilhrm.API_ERP.auth.dto.RefreshRequest;
import com.devguilhrm.API_ERP.auth.service.AuthService;
import com.devguilhrm.API_ERP.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticacao", description = "Login e renovacao de tokens")
@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@Operation(summary = "Realiza login", description = "Autentica usuario e retorna access token e refresh token")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login realizado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciais invalidas")
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Login realizado com sucesso", authService.login(request)));
	}

	@Operation(summary = "Renova token", description = "Gera novo access token a partir de refresh token persistido")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token renovado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token invalido")
	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Token renovado com sucesso", authService.refresh(request.refreshToken())));
	}

	@Operation(summary = "Revoga refresh token", description = "Encerra a sessao associada ao refresh token informado")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout realizado")
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
			@Valid @RequestBody RefreshRequest request,
			@RequestHeader(value = "Authorization", required = false) String authorization
	) {
		authService.logout(request.refreshToken(), bearerToken(authorization));
		return ResponseEntity.ok(ApiResponse.success("Logout realizado com sucesso", null));
	}

	private String bearerToken(String authorization) {
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return null;
		}
		return authorization.substring(7);
	}
}
