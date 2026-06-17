package com.devguilhrm.API_ERP.security;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserDetailsServiceImpl userDetailsService;

	@Test
	void loadUserByUsernameShouldReturnPrincipal() {
		User user = User.builder()
				.name("Seller")
				.email("seller@crm.com")
				.password("hash")
				.role(Role.SELLER)
				.enabled(true)
				.build();
		user.setId(UUID.randomUUID());
		when(userRepository.findByEmail("seller@crm.com")).thenReturn(Optional.of(user));

		var result = userDetailsService.loadUserByUsername("seller@crm.com");

		assertThat(result).isInstanceOf(UserPrincipal.class);
		assertThat(result.getUsername()).isEqualTo("seller@crm.com");
		assertThat(result.getPassword()).isEqualTo("hash");
		assertThat(result.getAuthorities()).extracting("authority").containsExactly("ROLE_SELLER");
	}

	@Test
	void loadUserByUsernameShouldThrowWhenUserDoesNotExist() {
		when(userRepository.findByEmail("missing@crm.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@crm.com"))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessage("Usuario nao encontrado");
	}
}
