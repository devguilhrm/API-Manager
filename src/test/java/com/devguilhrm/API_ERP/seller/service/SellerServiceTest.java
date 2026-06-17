package com.devguilhrm.API_ERP.seller.service;

import com.devguilhrm.API_ERP.auth.dto.CreateUserRequest;
import com.devguilhrm.API_ERP.auth.dto.UserDTO;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.service.AuthService;
import com.devguilhrm.API_ERP.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

	@Mock
	private AuthService authService;

	@InjectMocks
	private SellerServiceImpl sellerService;

	@Test
	void createSellerShouldDelegateWhenRoleIsSeller() {
		var request = new CreateUserRequest("Seller", "seller@crm.com", "123456", Role.SELLER);
		var dto = new UserDTO(UUID.randomUUID(), "Seller", "seller@crm.com", Role.SELLER, true);
		when(authService.createUser(request)).thenReturn(dto);

		var result = sellerService.createSeller(request);

		assertThat(result).isEqualTo(dto);
		verify(authService).createUser(request);
	}

	@Test
	void createSellerShouldRejectNonSellerRole() {
		var request = new CreateUserRequest("Manager", "manager@crm.com", "123456", Role.MANAGER);

		assertThatThrownBy(() -> sellerService.createSeller(request))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Endpoint permite criar apenas vendedores");
		verifyNoInteractions(authService);
	}

	@Test
	void listShouldDelegateToAuthService() {
		var pageable = PageRequest.of(0, 10);
		var dto = new UserDTO(UUID.randomUUID(), "Seller", "seller@crm.com", Role.SELLER, true);
		when(authService.listSellers(pageable)).thenReturn(new PageImpl<>(List.of(dto)));

		var result = sellerService.list(pageable);

		assertThat(result.getContent()).containsExactly(dto);
	}
}
