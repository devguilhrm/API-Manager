package com.devguilhrm.API_ERP.sale.repository;

import com.devguilhrm.API_ERP.sale.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {
}
