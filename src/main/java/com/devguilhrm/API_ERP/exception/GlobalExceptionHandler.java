package com.devguilhrm.API_ERP.exception;

import com.devguilhrm.API_ERP.common.response.ErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
		return build(HttpStatus.NOT_FOUND, "Recurso nao encontrado", ex.getMessage());
	}

	@ExceptionHandler(InsufficientStockException.class)
	public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
		return build(HttpStatus.CONFLICT, "Estoque insuficiente", ex.getMessage());
	}

	@ExceptionHandler(UnauthorizedOperationException.class)
	public ResponseEntity<ErrorResponse> handleUnauthorizedOperation(UnauthorizedOperationException ex) {
		return build(HttpStatus.FORBIDDEN, "Operacao nao autorizada", ex.getMessage());
	}

	@ExceptionHandler({BusinessException.class, ConstraintViolationException.class})
	public ResponseEntity<ErrorResponse> handleBusiness(RuntimeException ex) {
		return build(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negocio violada", ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.toList();
		return ResponseEntity.badRequest().body(ErrorResponse.of("Dados invalidos", errors));
	}

	@ExceptionHandler({BadCredentialsException.class, JwtException.class})
	public ResponseEntity<ErrorResponse> handleUnauthorized(RuntimeException ex) {
		return build(HttpStatus.UNAUTHORIZED, "Credenciais invalidas", ex.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
		return build(HttpStatus.FORBIDDEN, "Acesso negado", ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
		log.error("Erro inesperado", ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Falha inesperada ao processar requisicao");
	}

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String error) {
		if (status.is5xxServerError()) {
			log.error("{}: {}", message, error);
		} else {
			log.warn("{}: {}", message, error);
		}
		return ResponseEntity.status(status).body(ErrorResponse.of(message, error));
	}
}
