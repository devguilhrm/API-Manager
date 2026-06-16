package com.devguilhrm.API_ERP.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

	private final SecretKey secretKey;
	private final long accessTokenExpirationMinutes;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.access-token-expiration-minutes}") long accessTokenExpirationMinutes
	) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
	}

	public String generateAccessToken(UserPrincipal principal) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(principal.getUsername())
				.claims(Map.of(
						"userId", principal.id().toString(),
						"role", principal.role().name()
				))
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES)))
				.signWith(secretKey)
				.compact();
	}

	public String extractUsername(String token) {
		return claims(token).getSubject();
	}

	public boolean isValid(String token, UserPrincipal principal) {
		return extractUsername(token).equals(principal.getUsername())
				&& claims(token).getExpiration().after(new Date());
	}

	private Claims claims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
