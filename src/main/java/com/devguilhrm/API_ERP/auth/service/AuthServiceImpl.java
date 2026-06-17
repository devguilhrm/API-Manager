package com.devguilhrm.API_ERP.auth.service;

import com.devguilhrm.API_ERP.auth.dto.AuthResponse;
import com.devguilhrm.API_ERP.auth.dto.CreateUserRequest;
import com.devguilhrm.API_ERP.auth.dto.LoginRequest;
import com.devguilhrm.API_ERP.auth.dto.UserDTO;
import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.auth.enums.Role;
import com.devguilhrm.API_ERP.auth.repository.UserRepository;
import com.devguilhrm.API_ERP.exception.BusinessException;
import com.devguilhrm.API_ERP.exception.ResourceNotFoundException;
import com.devguilhrm.API_ERP.refreshToken.entity.RefreshToken;
import com.devguilhrm.API_ERP.refreshToken.service.RefreshTokenService;
import com.devguilhrm.API_ERP.security.JwtService;
import com.devguilhrm.API_ERP.security.TokenBlacklistService;
import com.devguilhrm.API_ERP.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final LoginRateLimitService loginRateLimitService;
	private final TokenBlacklistService tokenBlacklistService;

	public AuthServiceImpl(
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			RefreshTokenService refreshTokenService,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			LoginRateLimitService loginRateLimitService,
			TokenBlacklistService tokenBlacklistService
	) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.loginRateLimitService = loginRateLimitService;
		this.tokenBlacklistService = tokenBlacklistService;
	}

	@Override
	@Transactional
	public AuthResponse login(LoginRequest request) {
		log.info("Login solicitado para {}", request.email());
		loginRateLimitService.ensureAllowed(request.email());
		Authentication authentication;
		try {
			authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.email(), request.password())
			);
			loginRateLimitService.reset(request.email());
		} catch (BadCredentialsException ex) {
			loginRateLimitService.recordFailure(request.email());
			throw ex;
		}
		UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
		RefreshToken refreshToken = refreshTokenService.create(principal.user());
		return response(principal, refreshTokenValue(refreshToken));
	}

	@Override
	@Transactional
	public AuthResponse refresh(String refreshTokenValue) {
		RefreshToken refreshToken = refreshTokenService.rotate(refreshTokenValue);
		UserPrincipal principal = new UserPrincipal(refreshToken.getUser());
		return response(principal, refreshTokenValue(refreshToken));
	}

	@Override
	@Transactional
	public void logout(String refreshTokenValue, String accessToken) {
		refreshTokenService.revoke(refreshTokenValue);
		if (accessToken != null && !accessToken.isBlank()) {
			tokenBlacklistService.blacklist(accessToken, jwtService.extractExpiration(accessToken));
		}
	}

	@Override
	@Transactional
	public UserDTO createUser(CreateUserRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			log.warn("Tentativa de criar usuario com email duplicado {}", request.email());
			throw new BusinessException("Ja existe usuario com este email");
		}
		User user = User.builder()
				.name(request.name())
				.email(request.email())
				.password(passwordEncoder.encode(request.password()))
				.role(request.role())
				.enabled(true)
				.build();
		log.info("Criando usuario {} com perfil {}", request.email(), request.role());
		return toDto(userRepository.save(user));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<UserDTO> listSellers(Pageable pageable) {
		return userRepository.findAllByRole(Role.SELLER, pageable).map(this::toDto);
	}

	@Override
	@Transactional(readOnly = true)
	public User getAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
			throw new ResourceNotFoundException("Usuario autenticado", "contexto");
		}
		return principal.user();
	}

	private AuthResponse response(UserPrincipal principal, String refreshToken) {
		return new AuthResponse(
				jwtService.generateAccessToken(principal),
				refreshToken,
				principal.id(),
				principal.user().getName(),
				principal.user().getEmail(),
				principal.role()
		);
	}

	private String refreshTokenValue(RefreshToken refreshToken) {
		return refreshToken.getRawToken() == null ? refreshToken.getToken() : refreshToken.getRawToken();
	}

	private UserDTO toDto(User user) {
		return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isEnabled());
	}
}
