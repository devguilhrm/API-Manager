package com.devguilhrm.API_ERP.clients.service;

import com.devguilhrm.API_ERP.clients.dto.ClientDTO;
import com.devguilhrm.API_ERP.clients.dto.CreateClientRequest;
import com.devguilhrm.API_ERP.clients.dto.ReassignClientRequest;
import com.devguilhrm.API_ERP.clients.dto.UpdateClientRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ClientService {

	ClientDTO create(CreateClientRequest request);

	ClientDTO update(UUID id, UpdateClientRequest request);

	ClientDTO getById(UUID id);

	default Page<ClientDTO> list(Pageable pageable) {
		return list(null, null, pageable);
	}

	Page<ClientDTO> list(String search, UUID sellerId, Pageable pageable);

	ClientDTO reassign(UUID id, ReassignClientRequest request);
}
