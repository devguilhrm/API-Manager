package com.devguilhrm.API_ERP.clients.service;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.repository.UserRepository;
import com.devguilhrm.API_ERP.auth.service.AuthService;
import com.devguilhrm.API_ERP.clients.dto.ClientDTO;
import com.devguilhrm.API_ERP.clients.dto.CreateClientRequest;
import com.devguilhrm.API_ERP.clients.entity.Client;
import com.devguilhrm.API_ERP.clients.mapper.ClientMapper;
import com.devguilhrm.API_ERP.clients.repository.ClientRepository;
import com.devguilhrm.API_ERP.exception.UnauthorizedOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

	@Mock
	private ClientRepository clientRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ClientMapper clientMapper;

	@Mock
	private AuthService authService;

	@InjectMocks
	private ClientServiceImpl clientService;

	private User seller;
	private Client client;

	@BeforeEach
	void setUp() {
		seller = User.builder().name("Seller").email("seller@crm.com").role(Role.SELLER).enabled(true).build();
		seller.setId(UUID.randomUUID());
		client = Client.builder().name("Client").email("client@crm.com").phone("999").seller(seller).build();
		client.setId(UUID.randomUUID());
	}

	@Test
	void createShouldBindAuthenticatedSeller() {
		when(authService.getAuthenticatedUser()).thenReturn(seller);
		when(clientMapper.toEntity(new CreateClientRequest("Client", "client@crm.com", "999", null))).thenReturn(client);
		when(clientRepository.save(client)).thenReturn(client);
		when(clientMapper.toDto(client)).thenReturn(dto(client));

		var result = clientService.create(new CreateClientRequest("Client", "client@crm.com", "999", null));

		assertThat(result.sellerId()).isEqualTo(seller.getId());
	}

	@Test
	void sellerListShouldOnlyUseSellerClients() {
		when(authService.getAuthenticatedUser()).thenReturn(seller);
		when(clientRepository.findAllBySellerId(seller.getId(), PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(client)));
		when(clientMapper.toDto(client)).thenReturn(dto(client));

		var page = clientService.list(PageRequest.of(0, 10));

		assertThat(page.getContent()).hasSize(1);
	}

	@Test
	void sellerCannotReadAnotherSellerClient() {
		User otherSeller = User.builder().name("Other").email("other@crm.com").role(Role.SELLER).enabled(true).build();
		otherSeller.setId(UUID.randomUUID());
		client.setSeller(otherSeller);
		when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
		when(authService.getAuthenticatedUser()).thenReturn(seller);

		assertThatThrownBy(() -> clientService.getById(client.getId()))
				.isInstanceOf(UnauthorizedOperationException.class);
	}

	private ClientDTO dto(Client source) {
		return new ClientDTO(source.getId(), source.getName(), source.getEmail(), source.getPhone(), source.getAddress(),
				source.getSeller().getId(), source.getSeller().getName(), null, null);
	}
}
