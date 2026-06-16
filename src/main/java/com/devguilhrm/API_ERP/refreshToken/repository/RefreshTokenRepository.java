package com.devguilhrm.API_ERP.refreshToken.repository;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.refreshToken.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	Optional<RefreshToken> findByToken(String token);

	@Modifying
	@Query("update RefreshToken rt set rt.revoked = true where rt.user = :user and rt.revoked = false")
	void revokeActiveTokensByUser(User user);
}
