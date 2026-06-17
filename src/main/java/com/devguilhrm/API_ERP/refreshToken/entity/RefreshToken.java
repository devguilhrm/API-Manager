package com.devguilhrm.API_ERP.refreshToken.entity;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

	@Column(nullable = false, unique = true, length = 120)
	private String token;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private boolean revoked;

	@Transient
	private String rawToken;

	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}
}
