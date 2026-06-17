package com.devguilhrm.API_ERP.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public RedisCacheManager cacheManager(
			RedisConnectionFactory connectionFactory,
			@Value("${app.cache.ttl-minutes}") long ttlMinutes
	) {
		RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofMinutes(ttlMinutes))
				.disableCachingNullValues();

		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(configuration)
				.build();
	}
}
