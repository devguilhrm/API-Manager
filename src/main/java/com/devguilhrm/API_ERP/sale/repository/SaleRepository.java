package com.devguilhrm.API_ERP.sale.repository;

import com.devguilhrm.API_ERP.sale.entity.Sale;
import com.devguilhrm.API_ERP.sale.enums.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

	@Query("select s from Sale s where s.seller.id = :sellerId")
	Page<Sale> findAllBySellerId(UUID sellerId, Pageable pageable);

	@Query("select coalesce(sum(s.totalAmount), 0) from Sale s where s.status = :status")
	BigDecimal sumTotalByStatus(SaleStatus status);

	@Query("select s.seller.name, coalesce(sum(s.totalAmount), 0) from Sale s where s.status = :status group by s.seller.name")
	List<Object[]> sumRevenueBySeller(SaleStatus status);
}
