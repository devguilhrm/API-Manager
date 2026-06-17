package com.devguilhrm.API_ERP.sale.entity;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.common.entity.BaseEntity;
import com.devguilhrm.API_ERP.sale.enums.SaleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sale_status_history")
public class SaleStatusHistory extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "sale_id", nullable = false)
	private Sale sale;

	@Enumerated(EnumType.STRING)
	@Column(name = "previous_status")
	private SaleStatus previousStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "new_status", nullable = false)
	private SaleStatus newStatus;

	@Column(length = 500)
	private String reason;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "changed_by_user_id", nullable = false)
	private User changedBy;
}
