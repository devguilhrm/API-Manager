package com.devguilhrm.API_ERP.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
public abstract class InfrastructureTestContainers {

	@Container
	static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
			.withExposedPorts(6379);

	@Container
	static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

	@DynamicPropertySource
	static void infrastructureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
		registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
	}
}
