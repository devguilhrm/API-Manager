package com.devguilhrm.API_ERP.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Test
	void blacklistShouldStoreTokenWithRemainingTtl() {
		TokenBlacklistService service = new TokenBlacklistService(redisTemplate);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);

		service.blacklist("token", Instant.now().plus(Duration.ofHours(1)));

		verify(valueOperations).set(eq("jwt:blacklist:token"), eq("revoked"), ttlCaptor.capture());
		assertThat(ttlCaptor.getValue()).isPositive();
		assertThat(ttlCaptor.getValue()).isLessThanOrEqualTo(Duration.ofHours(1));
	}

	@Test
	void blacklistShouldIgnoreBlankToken() {
		TokenBlacklistService service = new TokenBlacklistService(redisTemplate);

		service.blacklist(" ", Instant.now().plus(Duration.ofHours(1)));

		verifyNoInteractions(redisTemplate);
	}

	@Test
	void blacklistShouldIgnoreExpiredToken() {
		TokenBlacklistService service = new TokenBlacklistService(redisTemplate);

		service.blacklist("token", Instant.now().minus(Duration.ofMinutes(1)));

		verify(redisTemplate, never()).opsForValue();
	}

	@Test
	void isBlacklistedShouldReadRedisKey() {
		TokenBlacklistService service = new TokenBlacklistService(redisTemplate);
		when(redisTemplate.hasKey("jwt:blacklist:token")).thenReturn(true);

		assertThat(service.isBlacklisted("token")).isTrue();
	}
}
