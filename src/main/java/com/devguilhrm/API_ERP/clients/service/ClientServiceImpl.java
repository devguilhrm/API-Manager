package com.devguilhrm.API_ERP.clients.service;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.repository.UserRepository;
import com.devguilhrm.API_ERP.auth.service.AuthService;
import com.devguilhrm.API_ERP.clients.dto.ClientDTO;
import com.devguilhrm.API_ERP.clients.dto.CreateClientRequest;
import com.devguilhrm.API_ERP.clients.dto.ReassignClientRequest;
import com.devguilhrm.API_ERP.clients.dto.UpdateClientRequest;
import com.devguilhrm.API_ERP.clients.entity.Client;
import com.devguilhrm.API_ERP.clients.mapper.ClientMapper;
import com.devguilhrm.API_ERP.clients.repository.ClientRepository;
import com.devguilhrm.API_ERP.exception.BusinessException;
import com.devguilhrm.API_ERP.exception.ResourceNotFoundException;
import com.devguilhrm.API_ERP.exception.UnauthorizedOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClientServiceImpl implements ClientService {

	private static final Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);

	private final ClientRepository clientRepository;
	private final UserRepository userRepository;
	private final ClientMapper clientMapper;
	private final AuthService authService;

	public ClientServiceImpl(
			ClientRepository clientRepository,
			UserRepository userRepository,
			ClientMapper clientMapper,
			AuthService authService
	) {
		this.clientRepository = clientRepository;
		this.userRepository = userRepository;
		this.clientMapper = clientMapper;
		this.authService = authService;
	}

	@Override
	@CacheEvict(value = "clients", allEntries = true)
	@Transactional
	public ClientDTO create(CreateClientRequest request) {
		if (clientRepository.existsByEmail(request.email())) {
			log.warn("Tentativa de cadastrar cliente com email duplicado {}", request.email());
			throw new BusinessException("Ja existe cliente com este email");
		}
		User seller = authService.getAuthenticatedUser();
		if (seller.getRole() != Role.SELLER) {
			throw new UnauthorizedOperationException("Apenas vendedores podem cadastrar clientes diretamente");
		}
		Client client = clientMapper.toEntity(request);
		client.setSeller(seller);
		log.info("Criando cliente {} para vendedor {}", request.email(), seller.getEmail());
		return clientMapper.toDto(clientRepository.save(client));
	}

	@Override
	@CacheEvict(value = "clients", allEntries = true)
	@Transactional
	public ClientDTO update(UUID id, UpdateClientRequest request) {
		Client client = findVisibleClient(id);
		if (clientRepository.existsByEmailAndIdNot(request.email(), id)) {
			throw new BusinessException("Ja existe cliente com este email");
		}
		clientMapper.update(request, client);
		log.info("Atualizando cliente {}", id);
		return clientMapper.toDto(clientRepository.save(client));
	}

	@Override
	@Transactional(readOnly = true)
	public ClientDTO getById(UUID id) {
		return clientMapper.toDto(findVisibleClient(id));
	}

	@Override
	@Cacheable(
			value = "clients",
			key = "{#search, #sellerId, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString()}"
	)
	@Transactional(readOnly = true)
	public Page<ClientDTO> list(String search, UUID sellerId, Pageable pageable) {
		User user = authService.getAuthenticatedUser();
		if (user.getRole() == Role.SELLER) {
			return clientRepository.search(normalize(search), user.getId(), pageable).map(clientMapper::toDto);
		}
		return clientRepository.search(normalize(search), sellerId, pageable).map(clientMapper::toDto);
	}

	@Override
	@CacheEvict(value = "clients", allEntries = true)
	@Transactional
	public ClientDTO reassign(UUID id, ReassignClientRequest request) {
		Client client = clientRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
		User seller = userRepository.findById(request.sellerId())
				.orElseThrow(() -> new ResourceNotFoundException("Vendedor", request.sellerId()));
		if (seller.getRole() != Role.SELLER) {
			throw new BusinessException("Usuario informado nao e vendedor");
		}
		client.setSeller(seller);
		log.info("Reatribuindo cliente {} para vendedor {}", id, seller.getEmail());
		return clientMapper.toDto(clientRepository.save(client));
	}

	private Client findVisibleClient(UUID id) {
		Client client = clientRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
		User user = authService.getAuthenticatedUser();
		if (user.getRole() == Role.SELLER && !client.getSeller().getId().equals(user.getId())) {
			log.warn("Vendedor {} tentou acessar cliente {}", user.getEmail(), id);
			throw new UnauthorizedOperationException("Cliente pertence a outro vendedor");
		}
		return client;
	}

	private String normalize(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
