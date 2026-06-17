package com.devguilhrm.API_ERP.sale.repository;

import com.devguilhrm.API_ERP.sale.entity.Sale;
import com.devguilhrm.API_ERP.sale.enums.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

	@Query("select s from Sale s where s.seller.id = :sellerId")
	Page<Sale> findAllBySellerId(UUID sellerId, Pageable pageable);

	@Query("""
			select s from Sale s
			where (:sellerId is null or s.seller.id = :sellerId)
			  and (:status is null or s.status = :status)
			  and (:fromDate is null or s.createdAt >= :fromDate)
			  and (:toDate is null or s.createdAt < :toDate)
			""")
	Page<Sale> search(SaleStatus status, UUID sellerId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

	@Query("select coalesce(sum(s.totalAmount), 0) from Sale s where s.status = :status")
	BigDecimal sumTotalByStatus(SaleStatus status);

	@Query("select s.seller.name, coalesce(sum(s.totalAmount), 0) from Sale s where s.status = :status group by s.seller.name")
	List<Object[]> sumRevenueBySeller(SaleStatus status);

	@Query("""
			select count(s) from Sale s
			where (:fromDate is null or s.createdAt >= :fromDate)
			  and (:toDate is null or s.createdAt < :toDate)
			""")
	long countByPeriod(LocalDateTime fromDate, LocalDateTime toDate);

	@Query("""
			select coalesce(sum(s.totalAmount), 0) from Sale s
			where s.status = :status
			  and (:fromDate is null or s.createdAt >= :fromDate)
			  and (:toDate is null or s.createdAt < :toDate)
			""")
	BigDecimal sumTotalByStatusAndPeriod(SaleStatus status, LocalDateTime fromDate, LocalDateTime toDate);

	@Query("""
			select s.seller.name, coalesce(sum(s.totalAmount), 0)
			from Sale s
			where s.status = :status
			  and (:fromDate is null or s.createdAt >= :fromDate)
			  and (:toDate is null or s.createdAt < :toDate)
			group by s.seller.name
			""")
	List<Object[]> sumRevenueBySellerAndPeriod(SaleStatus status, LocalDateTime fromDate, LocalDateTime toDate);
}
