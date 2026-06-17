package com.devguilhrm.API_ERP.clients.repository;

import com.devguilhrm.API_ERP.clients.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

	boolean existsByEmail(String email);

	boolean existsByEmailAndIdNot(String email, UUID id);

	@Query("select c from Client c where c.seller.id = :sellerId")
	Page<Client> findAllBySellerId(UUID sellerId, Pageable pageable);

	@Query("""
			select c from Client c
			where (:sellerId is null or c.seller.id = :sellerId)
			  and (
					:search is null
					or lower(c.name) like lower(concat('%', :search, '%'))
					or lower(c.email) like lower(concat('%', :search, '%'))
					or c.phone like concat('%', :search, '%')
			  )
			""")
	Page<Client> search(String search, UUID sellerId, Pageable pageable);
}
