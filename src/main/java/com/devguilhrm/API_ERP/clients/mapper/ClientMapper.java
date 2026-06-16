package com.devguilhrm.API_ERP.clients.mapper;

import com.devguilhrm.API_ERP.clients.dto.ClientDTO;
import com.devguilhrm.API_ERP.clients.dto.CreateClientRequest;
import com.devguilhrm.API_ERP.clients.dto.UpdateClientRequest;
import com.devguilhrm.API_ERP.clients.entity.Client;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ClientMapper {

	@Mapping(target = "sellerId", source = "seller.id")
	@Mapping(target = "sellerName", source = "seller.name")
	ClientDTO toDto(Client client);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "seller", ignore = true)
	Client toEntity(CreateClientRequest request);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "seller", ignore = true)
	void update(UpdateClientRequest request, @MappingTarget Client client);
}
