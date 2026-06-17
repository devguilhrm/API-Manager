package com.devguilhrm.API_ERP.sale.repository;

import com.devguilhrm.API_ERP.sale.entity.SaleItem;
import com.devguilhrm.API_ERP.sale.enums.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {

	@Query("""
			select coalesce(sum(i.quantity), 0)
			from SaleItem i
			where i.product.id = :productId
			  and i.sale.status = :status
			""")
	Long sumQuantityByProductIdAndSaleStatus(UUID productId, SaleStatus status);
}
