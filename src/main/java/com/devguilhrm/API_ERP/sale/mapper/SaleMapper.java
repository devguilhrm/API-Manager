package com.devguilhrm.API_ERP.sale.mapper;

import com.devguilhrm.API_ERP.sale.dto.SaleDTO;
import com.devguilhrm.API_ERP.sale.dto.SaleItemDTO;
import com.devguilhrm.API_ERP.sale.entity.Sale;
import com.devguilhrm.API_ERP.sale.entity.SaleItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SaleMapper {

	@Mapping(target = "clientId", source = "client.id")
	@Mapping(target = "clientName", source = "client.name")
	@Mapping(target = "sellerId", source = "seller.id")
	@Mapping(target = "sellerName", source = "seller.name")
	SaleDTO toDto(Sale sale);

	@Mapping(target = "productId", source = "product.id")
	SaleItemDTO toDto(SaleItem item);
}
