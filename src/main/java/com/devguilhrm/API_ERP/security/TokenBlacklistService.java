package com.devguilhrm.API_ERP.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenBlacklistService {

	private static final String PREFIX = "jwt:blacklist:";

	private final StringRedisTemplate redisTemplate;

	public TokenBlacklistService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void blacklist(String token, Instant expiresAt) {
		if (token == null || token.isBlank()) {
			return;
		}
		Duration ttl = Duration.between(Instant.now(), expiresAt);
		if (!ttl.isNegative() && !ttl.isZero()) {
			redisTemplate.opsForValue().set(key(token), "revoked", ttl);
		}
	}

	public boolean isBlacklisted(String token) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(key(token)));
	}

	private String key(String token) {
		return PREFIX + token;
	}
}
