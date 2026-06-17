package com.devguilhrm.API_ERP.refreshToken.service;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.exception.UnauthorizedOperationException;
import com.devguilhrm.API_ERP.refreshToken.entity.RefreshToken;
import com.devguilhrm.API_ERP.refreshToken.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	private RefreshTokenServiceImpl refreshTokenService;

	@BeforeEach
	void setUp() {
		this.refreshTokenService = new RefreshTokenServiceImpl(refreshTokenRepository, 7);
	}

	@Test
	void createShouldRevokeActiveTokensAndStoreHashedToken() {
		User user = user();
		Instant beforeExpiration = Instant.now().plus(7, ChronoUnit.DAYS);
		when(refreshTokenRepository.save(any(RefreshToken.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		RefreshToken token = refreshTokenService.create(user);

		verify(refreshTokenRepository).revokeActiveTokensByUser(user);
		assertThat(token.getRawToken()).isNotBlank();
		assertThat(token.getToken()).isEqualTo(hash(token.getRawToken()));
		assertThat(token.getToken()).isNotEqualTo(token.getRawToken());
		assertThat(token.isRevoked()).isFalse();
		assertThat(token.getExpiresAt()).isAfterOrEqualTo(beforeExpiration);
	}

	@Test
	void validateShouldReturnTokenWhenHashExistsAndTokenIsActive() {
		RefreshToken storedToken = RefreshToken.builder()
				.user(user())
				.token(hash("raw-token"))
				.expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
				.revoked(false)
				.build();
		when(refreshTokenRepository.findByToken(hash("raw-token"))).thenReturn(Optional.of(storedToken));

		RefreshToken result = refreshTokenService.validate("raw-token");

		assertThat(result).isSameAs(storedToken);
	}

	@Test
	void validateShouldRejectMissingToken() {
		when(refreshTokenRepository.findByToken(hash("missing"))).thenReturn(Optional.empty());

		assertThatThrownBy(() -> refreshTokenService.validate("missing"))
				.isInstanceOf(UnauthorizedOperationException.class)
				.hasMessage("Refresh token invalido");
	}

	@Test
	void validateShouldRejectRevokedToken() {
		RefreshToken storedToken = RefreshToken.builder()
				.user(user())
				.token(hash("raw-token"))
				.expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
				.revoked(true)
				.build();
		when(refreshTokenRepository.findByToken(hash("raw-token"))).thenReturn(Optional.of(storedToken));

		assertThatThrownBy(() -> refreshTokenService.validate("raw-token"))
				.isInstanceOf(UnauthorizedOperationException.class)
				.hasMessage("Refresh token expirado ou revogado");
	}

	@Test
	void rotateShouldRevokeCurrentTokenAndCreateAnotherToken() {
		User user = user();
		RefreshToken current = RefreshToken.builder()
				.user(user)
				.token(hash("raw-token"))
				.expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
				.revoked(false)
				.build();
		when(refreshTokenRepository.findByToken(hash("raw-token"))).thenReturn(Optional.of(current));
		when(refreshTokenRepository.save(any(RefreshToken.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		RefreshToken rotated = refreshTokenService.rotate("raw-token");

		assertThat(current.isRevoked()).isTrue();
		assertThat(rotated.getUser()).isSameAs(user);
		assertThat(rotated.getRawToken()).isNotBlank();
		verify(refreshTokenRepository).save(current);
		verify(refreshTokenRepository).revokeActiveTokensByUser(user);
	}

	@Test
	void revokeShouldMarkTokenAsRevoked() {
		RefreshToken storedToken = RefreshToken.builder()
				.user(user())
				.token(hash("raw-token"))
				.expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
				.revoked(false)
				.build();
		when(refreshTokenRepository.findByToken(hash("raw-token"))).thenReturn(Optional.of(storedToken));

		refreshTokenService.revoke("raw-token");

		assertThat(storedToken.isRevoked()).isTrue();
		verify(refreshTokenRepository).save(storedToken);
	}

	private User user() {
		User user = User.builder()
				.name("User")
				.email("user@crm.com")
				.password("hash")
				.role(Role.SELLER)
				.enabled(true)
				.build();
		user.setId(UUID.randomUUID());
		return user;
	}

	private String hash(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}
}
