package com.devguilhrm.API_ERP.auth.controller;

import com.devguilhrm.API_ERP.auth.dto.AuthResponse;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.service.AuthService;
import com.devguilhrm.API_ERP.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	@Mock
	private AuthService authService;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator())
				.build();
	}

	@Test
	void loginShouldReturnTokens() throws Exception {
		UUID userId = UUID.randomUUID();
		when(authService.login(org.mockito.ArgumentMatchers.any()))
				.thenReturn(new AuthResponse("access", "refresh", userId, "Admin", "admin@crm.com", Role.MANAGER));

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "admin@crm.com",
								  "password": "admin123"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Login realizado com sucesso"))
				.andExpect(jsonPath("$.data.accessToken").value("access"))
				.andExpect(jsonPath("$.data.refreshToken").value("refresh"))
				.andExpect(jsonPath("$.data.userId").value(userId.toString()))
				.andExpect(jsonPath("$.data.role").value("MANAGER"));
	}

	@Test
	void loginShouldValidateRequestBody() throws Exception {
		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "invalid"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Dados invalidos"))
				.andExpect(jsonPath("$.errors", hasItem(containsString("email"))));
	}

	@Test
	void logoutShouldPassRefreshAndBearerTokenToService() throws Exception {
		mockMvc.perform(post("/auth/logout")
						.header("Authorization", "Bearer access-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(java.util.Map.of("refreshToken", "refresh-token"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Logout realizado com sucesso"));

		verify(authService).logout("refresh-token", "access-token");
	}

	private LocalValidatorFactoryBean validator() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		return validator;
	}
}
