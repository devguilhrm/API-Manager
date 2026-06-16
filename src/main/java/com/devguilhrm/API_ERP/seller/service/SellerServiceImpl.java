package com.devguilhrm.API_ERP.seller.service;

import com.devguilhrm.API_ERP.auth.dto.CreateUserRequest;
import com.devguilhrm.API_ERP.auth.dto.UserDTO;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.service.AuthService;
import com.devguilhrm.API_ERP.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SellerServiceImpl implements SellerService {

	private static final Logger log = LoggerFactory.getLogger(SellerServiceImpl.class);

	private final AuthService authService;

	public SellerServiceImpl(AuthService authService) {
		this.authService = authService;
	}

	@Override
	@Transactional
	public UserDTO createSeller(CreateUserRequest request) {
		if (request.role() != Role.SELLER) {
			throw new BusinessException("Endpoint permite criar apenas vendedores");
		}
		log.info("Criando vendedor {}", request.email());
		return authService.createUser(request);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<UserDTO> list(Pageable pageable) {
		return authService.listSellers(pageable);
	}
}
