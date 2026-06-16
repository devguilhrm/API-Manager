package com.devguilhrm.API_ERP.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {

	public InsufficientStockException(UUID productId, int requested, int available) {
		super("Produto " + productId + ": solicitado " + requested + ", disponivel " + available);
	}
}
