package com.devguilhrm.API_ERP.seller.service;

import com.devguilhrm.API_ERP.auth.dto.CreateUserRequest;
import com.devguilhrm.API_ERP.auth.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SellerService {

	UserDTO createSeller(CreateUserRequest request);

	Page<UserDTO> list(Pageable pageable);
}
