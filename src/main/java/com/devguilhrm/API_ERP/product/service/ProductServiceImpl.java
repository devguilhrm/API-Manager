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
	@Transactional
	public ProductDTO create(CreateProductRequest request) {
		log.info("Criando produto {}", request.name());
		return productMapper.toDto(productRepository.save(productMapper.toEntity(request)));
	}

	@Override
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
	@Transactional(readOnly = true)
	public Page<ProductDTO> list(Pageable pageable) {
		return productRepository.findAll(pageable).map(productMapper::toDto);
	}

	@Override
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
}
