package com.devguilhrm.API_ERP.exception;

public class RateLimitExceededException extends RuntimeException {

	public RateLimitExceededException(String message) {
		super(message);
	}
}
