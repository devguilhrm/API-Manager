package com.devguilhrm.API_ERP.product.service;

import com.devguilhrm.API_ERP.exception.ResourceNotFoundException;
import com.devguilhrm.API_ERP.product.dto.CreateProductRequest;
import com.devguilhrm.API_ERP.product.dto.ProductDTO;
import com.devguilhrm.API_ERP.product.dto.UpdateProductRequest;
import com.devguilhrm.API_ERP.product.entity.Product;
import com.devguilhrm.API_ERP.product.mapper.ProductMapper;
import com.devguilhrm.API_ERP.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

	private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

	private final ProductRepository productRepository;
	private final ProductMapper productMapper;

	public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
		this.productRepository = productRepository;
		this.productMapper = productMapper;
	}

	@Override
	@CacheEvict(value = "products", allEntries = true)
	@Transactional
	public ProductDTO create(CreateProductRequest request) {
		log.info("Criando produto {}", request.name());
		return productMapper.toDto(productRepository.save(productMapper.toEntity(request)));
	}

	@Override
	@CacheEvict(value = "products", allEntries = true)
	@Transactional
	public ProductDTO update(UUID id, UpdateProductRequest request) {
		Product product = findEntity(id);
		productMapper.update(request, product);
		log.info("Atualizando produto {}", id);
		return productMapper.toDto(productRepository.save(product));
	}

	@Override
	@Transactional(readOnly = true)
	public ProductDTO getById(UUID id) {
		return productMapper.toDto(findEntity(id));
	}

	@Override
	@Cacheable(
			value = "products",
			key = "{#search, #active, #lowStockThreshold, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString()}"
	)
	@Transactional(readOnly = true)
	public Page<ProductDTO> list(String search, Boolean active, Integer lowStockThreshold, Pageable pageable) {
		Boolean effectiveActive = active == null ? Boolean.TRUE : active;
		return productRepository.search(normalize(search), effectiveActive, lowStockThreshold, pageable).map(productMapper::toDto);
	}

	@Override
	@CacheEvict(value = "products", allEntries = true)
	@Transactional
	public void delete(UUID id) {
		Product product = findEntity(id);
		product.setActive(false);
		log.info("Desativando produto {}", id);
		productRepository.save(product);
	}

	private Product findEntity(UUID id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Produto", id));
	}

	private String normalize(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
