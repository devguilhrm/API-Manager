package com.devguilhrm.API_ERP.config;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.bootstrap.manager.enabled", havingValue = "true")
public class BootstrapManagerInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(BootstrapManagerInitializer.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final String name;
	private final String email;
	private final String password;

	public BootstrapManagerInitializer(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.bootstrap.manager.name}") String name,
			@Value("${app.bootstrap.manager.email}") String email,
			@Value("${app.bootstrap.manager.password}") String password
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.name = name;
		this.email = email;
		this.password = password;
	}

	@Override
	public void run(String... args) {
		if (email == null || email.isBlank() || password == null || password.isBlank()) {
			throw new IllegalStateException("BOOTSTRAP_MANAGER_EMAIL e BOOTSTRAP_MANAGER_PASSWORD sao obrigatorios quando o bootstrap esta habilitado");
		}
		if (userRepository.existsByEmail(email)) {
			log.info("Gerente bootstrap {} ja existe", email);
			return;
		}
		log.info("Criando gerente bootstrap {}", email);
		userRepository.save(User.builder()
				.name(name)
				.email(email)
				.password(passwordEncoder.encode(password))
				.role(Role.MANAGER)
				.enabled(true)
				.build());
	}
}
