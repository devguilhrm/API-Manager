package com.devguilhrm.API_ERP.config;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootstrapManagerInitializerTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Test
	void runShouldCreateManagerWhenEmailDoesNotExist() {
		var initializer = new BootstrapManagerInitializer(
				userRepository,
				passwordEncoder,
				"Admin",
				"admin@crm.com",
				"secret"
		);
		when(userRepository.existsByEmail("admin@crm.com")).thenReturn(false);
		when(passwordEncoder.encode("secret")).thenReturn("hash");

		initializer.run();

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		assertThat(captor.getValue().getName()).isEqualTo("Admin");
		assertThat(captor.getValue().getEmail()).isEqualTo("admin@crm.com");
		assertThat(captor.getValue().getPassword()).isEqualTo("hash");
		assertThat(captor.getValue().getRole()).isEqualTo(Role.MANAGER);
		assertThat(captor.getValue().isEnabled()).isTrue();
	}

	@Test
	void runShouldSkipWhenManagerAlreadyExists() {
		var initializer = new BootstrapManagerInitializer(
				userRepository,
				passwordEncoder,
				"Admin",
				"admin@crm.com",
				"secret"
		);
		when(userRepository.existsByEmail("admin@crm.com")).thenReturn(true);

		initializer.run();

		verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void runShouldFailWhenEnabledWithoutCredentials() {
		var initializer = new BootstrapManagerInitializer(
				userRepository,
				passwordEncoder,
				"Admin",
				"",
				""
		);

		assertThatThrownBy(initializer::run)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("BOOTSTRAP_MANAGER_EMAIL e BOOTSTRAP_MANAGER_PASSWORD sao obrigatorios quando o bootstrap esta habilitado");
	}
}
