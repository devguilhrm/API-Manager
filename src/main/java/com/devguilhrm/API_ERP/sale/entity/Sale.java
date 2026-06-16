package com.devguilhrm.API_ERP.sale.entity;

import com.devguilhrm.API_ERP.auth.entity.User;
import com.devguilhrm.API_ERP.clients.entity.Client;
import com.devguilhrm.API_ERP.common.entity.BaseEntity;
import com.devguilhrm.API_ERP.common.enums.PaymentMethod;
import com.devguilhrm.API_ERP.sale.enums.SaleStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sales")
public class Sale extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "client_id", nullable = false)
	private Client client;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "seller_id", nullable = false)
	private User seller;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SaleStatus status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentMethod paymentMethod;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal discount;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal totalAmount;

	@Column(length = 500)
	private String cancelReason;

	@OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<SaleItem> items = new ArrayList<>();

	public void addItem(SaleItem item) {
		items.add(item);
		item.setSale(this);
	}
}
