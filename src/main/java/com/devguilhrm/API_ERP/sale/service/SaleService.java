package com.devguilhrm.API_ERP.sale.service;

import com.devguilhrm.API_ERP.sale.dto.CancelSaleRequest;
import com.devguilhrm.API_ERP.sale.dto.CreateSaleRequest;
import com.devguilhrm.API_ERP.sale.dto.SaleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SaleService {

	SaleDTO create(CreateSaleRequest request);

	SaleDTO complete(UUID id);

	SaleDTO cancel(UUID id, CancelSaleRequest request);

	SaleDTO getById(UUID id);

	Page<SaleDTO> list(Pageable pageable);
}
