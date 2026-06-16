package com.devguilhrm.API_ERP.sale.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelSaleRequest(
		@NotBlank(message = "Motivo do cancelamento e obrigatorio")
		String reason
) {
}
