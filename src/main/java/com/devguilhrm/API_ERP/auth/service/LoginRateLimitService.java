package com.devguilhrm.API_ERP.auth.service;

import com.devguilhrm.API_ERP.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LoginRateLimitService {

	private static final String PREFIX = "login:attempts:";

	private final StringRedisTemplate redisTemplate;
	private final long maxAttempts;
	private final Duration window;

	public LoginRateLimitService(
			StringRedisTemplate redisTemplate,
			@Value("${app.rate-limit.login.max-attempts}") long maxAttempts,
			@Value("${app.rate-limit.login.window-minutes}") long windowMinutes
	) {
		this.redisTemplate = redisTemplate;
		this.maxAttempts = maxAttempts;
		this.window = Duration.ofMinutes(windowMinutes);
	}

	public void ensureAllowed(String email) {
		String attempts = redisTemplate.opsForValue().get(key(email));
		if (attempts != null && Long.parseLong(attempts) >= maxAttempts) {
			throw new RateLimitExceededException("Muitas tentativas de login. Tente novamente mais tarde.");
		}
	}

	public void recordFailure(String email) {
		String key = key(email);
		Long attempts = redisTemplate.opsForValue().increment(key);
		if (attempts != null && attempts == 1L) {
			redisTemplate.expire(key, window);
		}
		if (attempts != null && attempts >= maxAttempts) {
			throw new RateLimitExceededException("Muitas tentativas de login. Tente novamente mais tarde.");
		}
	}

	public void reset(String email) {
		redisTemplate.delete(key(email));
	}

	private String key(String email) {
		return PREFIX + email.toLowerCase();
	}
}
