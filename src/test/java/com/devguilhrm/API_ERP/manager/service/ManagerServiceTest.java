package com.devguilhrm.API_ERP.manager.service;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

	@Mock
	private AuthService authService;

	@InjectMocks
	private ManagerServiceImpl managerService;

	@Test
	void currentManagerShouldReturnAuthenticatedUserData() {
		User manager = User.builder()
				.name("Manager")
				.email("manager@crm.com")
				.password("hash")
				.role(Role.MANAGER)
				.enabled(true)
				.build();
		manager.setId(UUID.randomUUID());
		when(authService.getAuthenticatedUser()).thenReturn(manager);

		var result = managerService.currentManager();

		assertThat(result.id()).isEqualTo(manager.getId());
		assertThat(result.name()).isEqualTo("Manager");
		assertThat(result.email()).isEqualTo("manager@crm.com");
		assertThat(result.role()).isEqualTo(Role.MANAGER);
		assertThat(result.enabled()).isTrue();
	}
}
