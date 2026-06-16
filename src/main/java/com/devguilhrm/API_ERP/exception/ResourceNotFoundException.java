package com.devguilhrm.API_ERP.exception;

public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String resource, Object id) {
		super(resource + " nao encontrado: " + id);
	}
}
