package com.devguilhrm.API_ERP.refreshToken.service;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.refreshToken.entity.RefreshToken;

public interface RefreshTokenService {

	RefreshToken create(User user);

	RefreshToken validate(String token);

	RefreshToken rotate(String token);

	void revoke(String token);
}
