package com.devguilhrm.API_ERP.auth.service;

import com.devguilhrm.API_ERP.auth.dto.AuthResponse;
import com.devguilhrm.API_ERP.auth.dto.CreateUserRequest;
import com.devguilhrm.API_ERP.auth.dto.LoginRequest;
import com.devguilhrm.API_ERP.auth.dto.UserDTO;
import com.devguilhrm.API_ERP.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthService {

	AuthResponse login(LoginRequest request);

	AuthResponse refresh(String refreshToken);

	default void logout(String refreshToken) {
		logout(refreshToken, null);
	}

	void logout(String refreshToken, String accessToken);

	UserDTO createUser(CreateUserRequest request);

	Page<UserDTO> listSellers(Pageable pageable);

	User getAuthenticatedUser();
}
