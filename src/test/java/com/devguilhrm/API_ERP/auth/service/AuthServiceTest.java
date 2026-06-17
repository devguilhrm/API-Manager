package com.devguilhrm.API_ERP.auth.service;

import com.devguilhrm.API_ERP.auth.dto.LoginRequest;
import com.devguilhrm.API_ERP.auth.dto.RefreshRequest;
import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.repository.UserRepository;
import com.devguilhrm.API_ERP.refreshToken.entity.RefreshToken;
import com.devguilhrm.API_ERP.refreshToken.service.RefreshTokenService;
import com.devguilhrm.API_ERP.security.JwtService;
import com.devguilhrm.API_ERP.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtService jwtService;

	@Mock
	private RefreshTokenService refreshTokenService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthServiceImpl authService;

	@Test
	void loginShouldReturnTokens() {
		User user = user();
		UserPrincipal principal = new UserPrincipal(user);
		Authentication authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(principal);
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
		when(refreshTokenService.create(user)).thenReturn(RefreshToken.builder().user(user).token("refresh").expiresAt(Instant.now()).build());
		when(jwtService.generateAccessToken(principal)).thenReturn("access");

		var response = authService.login(new LoginRequest("admin@crm.com", "admin123"));

		assertThat(response.accessToken()).isEqualTo("access");
		assertThat(response.refreshToken()).isEqualTo("refresh");
	}

	@Test
	void invalidLoginShouldThrow() {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("bad"));

		assertThatThrownBy(() -> authService.login(new LoginRequest("admin@crm.com", "wrong")))
				.isInstanceOf(BadCredentialsException.class);
	}

	@Test
	void refreshShouldReturnNewAccessToken() {
		User user = user();
		RefreshToken rotatedToken = RefreshToken.builder().user(user).token("hashed-refresh").expiresAt(Instant.now()).build();
		rotatedToken.setRawToken("new-refresh");
		when(refreshTokenService.rotate("refresh")).thenReturn(rotatedToken);
		when(jwtService.generateAccessToken(any(UserPrincipal.class))).thenReturn("access");

		var response = authService.refresh(new RefreshRequest("refresh").refreshToken());

		assertThat(response.accessToken()).isEqualTo("access");
		assertThat(response.refreshToken()).isEqualTo("new-refresh");
	}

	private User user() {
		User user = User.builder().name("Admin").email("admin@crm.com").password("hash").role(Role.MANAGER).enabled(true).build();
		user.setId(UUID.randomUUID());
		return user;
	}
}
