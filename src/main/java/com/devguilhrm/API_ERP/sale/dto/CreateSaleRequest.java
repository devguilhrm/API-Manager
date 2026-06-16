package com.devguilhrm.API_ERP.sale.dto;

import com.devguilhrm.API_ERP.common.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateSaleRequest(
		@NotNull(message = "Cliente e obrigatorio")
		UUID clientId,

		@NotNull(message = "Metodo de pagamento e obrigatorio")
		PaymentMethod paymentMethod,

		@DecimalMin(value = "0.0", message = "Desconto deve ser maior ou igual a zero")
		BigDecimal discount,

		@NotEmpty(message = "A venda deve ter pelo menos um item")
		List<@Valid CreateSaleItemRequest> items
) {
}
