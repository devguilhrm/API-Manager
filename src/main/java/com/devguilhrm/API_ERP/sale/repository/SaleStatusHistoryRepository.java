package com.devguilhrm.API_ERP.sale.repository;

import com.devguilhrm.API_ERP.sale.entity.SaleStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SaleStatusHistoryRepository extends JpaRepository<SaleStatusHistory, UUID> {
}
