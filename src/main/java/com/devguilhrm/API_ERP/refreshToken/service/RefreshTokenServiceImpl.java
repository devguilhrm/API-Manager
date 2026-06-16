package com.devguilhrm.API_ERP.refreshToken.service;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.exception.UnauthorizedOperationException;
import com.devguilhrm.API_ERP.refreshToken.entity.RefreshToken;
import com.devguilhrm.API_ERP.refreshToken.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

	private static final Logger log = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

	private final RefreshTokenRepository refreshTokenRepository;
	private final long refreshTokenExpirationDays;

	public RefreshTokenServiceImpl(
			RefreshTokenRepository refreshTokenRepository,
			@Value("${app.jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays
	) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.refreshTokenExpirationDays = refreshTokenExpirationDays;
	}

	@Override
	@Transactional
	public RefreshToken create(User user) {
		log.info("Criando refresh token para usuario {}", user.getEmail());
		refreshTokenRepository.revokeActiveTokensByUser(user);
		return refreshTokenRepository.save(RefreshToken.builder()
				.user(user)
				.token(UUID.randomUUID().toString())
				.expiresAt(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS))
				.revoked(false)
				.build());
	}

	@Override
	@Transactional(readOnly = true)
	public RefreshToken validate(String token) {
		RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
				.orElseThrow(() -> new UnauthorizedOperationException("Refresh token invalido"));
		if (refreshToken.isRevoked() || refreshToken.isExpired()) {
			log.warn("Refresh token invalido ou expirado");
			throw new UnauthorizedOperationException("Refresh token expirado ou revogado");
		}
		return refreshToken;
	}
}
