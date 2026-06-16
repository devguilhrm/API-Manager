package com.devguilhrm.API_ERP.product.service;

import com.devguilhrm.API_ERP.product.dto.CreateProductRequest;
import com.devguilhrm.API_ERP.product.dto.ProductDTO;
import com.devguilhrm.API_ERP.product.dto.UpdateProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {

	ProductDTO create(CreateProductRequest request);

	ProductDTO update(UUID id, UpdateProductRequest request);

	ProductDTO getById(UUID id);

	Page<ProductDTO> list(Pageable pageable);

	void delete(UUID id);
}
