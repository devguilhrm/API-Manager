package com.devguilhrm.API_ERP.auth.service;

import com.devguilhrm.API_ERP.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginRateLimitServiceTest {

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Test
	void ensureAllowedShouldPassWhenAttemptsAreBelowLimit() {
		LoginRateLimitService service = new LoginRateLimitService(redisTemplate, 3, 15);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get("login:attempts:user@crm.com")).thenReturn("2");

		service.ensureAllowed("USER@CRM.COM");

		verify(valueOperations).get("login:attempts:user@crm.com");
	}

	@Test
	void ensureAllowedShouldBlockWhenAttemptsReachedLimit() {
		LoginRateLimitService service = new LoginRateLimitService(redisTemplate, 3, 15);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get("login:attempts:user@crm.com")).thenReturn("3");

		assertThatThrownBy(() -> service.ensureAllowed("USER@CRM.COM"))
				.isInstanceOf(RateLimitExceededException.class);
	}

	@Test
	void recordFailureShouldExpireKeyOnFirstAttempt() {
		LoginRateLimitService service = new LoginRateLimitService(redisTemplate, 3, 15);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.increment("login:attempts:user@crm.com")).thenReturn(1L);

		service.recordFailure("USER@CRM.COM");

		verify(redisTemplate).expire("login:attempts:user@crm.com", Duration.ofMinutes(15));
	}

	@Test
	void recordFailureShouldBlockWhenAttemptsReachLimit() {
		LoginRateLimitService service = new LoginRateLimitService(redisTemplate, 3, 15);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.increment("login:attempts:user@crm.com")).thenReturn(3L);

		assertThatThrownBy(() -> service.recordFailure("USER@CRM.COM"))
				.isInstanceOf(RateLimitExceededException.class);
	}

	@Test
	void resetShouldDeleteAttemptsKey() {
		LoginRateLimitService service = new LoginRateLimitService(redisTemplate, 3, 15);

		service.reset("USER@CRM.COM");

		verify(redisTemplate).delete("login:attempts:user@crm.com");
	}
}
