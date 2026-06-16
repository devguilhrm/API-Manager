package com.devguilhrm.API_ERP.manager.service;

import com.devguilhrm.API_ERP.auth.dto.UserDTO;
import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManagerServiceImpl implements ManagerService {

	private static final Logger log = LoggerFactory.getLogger(ManagerServiceImpl.class);

	private final AuthService authService;

	public ManagerServiceImpl(AuthService authService) {
		this.authService = authService;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDTO currentManager() {
		User user = authService.getAuthenticatedUser();
		log.info("Consultando gerente autenticado {}", user.getEmail());
		return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isEnabled());
	}
}
