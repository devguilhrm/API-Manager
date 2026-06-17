package com.devguilhrm.API_ERP.sale.service;

import com.devguilhrm.API_ERP.sale.dto.CancelSaleRequest;
import com.devguilhrm.API_ERP.sale.dto.CreateSaleRequest;
import com.devguilhrm.API_ERP.sale.dto.SaleDTO;
import com.devguilhrm.API_ERP.sale.enums.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface SaleService {

	SaleDTO create(CreateSaleRequest request);

	SaleDTO complete(UUID id);

	SaleDTO cancel(UUID id, CancelSaleRequest request);

	SaleDTO getById(UUID id);

	default Page<SaleDTO> list(Pageable pageable) {
		return list(null, null, null, null, pageable);
	}

	Page<SaleDTO> list(SaleStatus status, UUID sellerId, LocalDate from, LocalDate to, Pageable pageable);
}
