package com.devguilhrm.API_ERP.common.response;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
		boolean success,
		String message,
		List<String> errors,
		LocalDateTime timestamp
) {

	public static ErrorResponse of(String message, List<String> errors) {
		return new ErrorResponse(false, message, errors, LocalDateTime.now());
	}

	public static ErrorResponse of(String message, String error) {
		return of(message, List.of(error));
	}
}
