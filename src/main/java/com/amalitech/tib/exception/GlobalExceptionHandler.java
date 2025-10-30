package com.amalitech.tib.exception;

import com.amalitech.tib.util.ApiResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Global exception handler for handling exceptions from all controllers. */
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                HashMap::new,
                (map, fieldError) -> map.put(fieldError.getField(), fieldError.getDefaultMessage()),
                HashMap::putAll);

    ApiResponse<Map<String, String>> errorResponse =
        ApiResponse.error("Validation Failed", List.of(errors));
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(
      IllegalArgumentException e) {
    ApiResponse<String> errorResponse = ApiResponse.error(e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(EmailAlreadyExistException.class)
  public ResponseEntity<ApiResponse<String>> handleEmailAlreadyExistException(
      EmailAlreadyExistException e) {
    ApiResponse<String> errorResponse = ApiResponse.error(e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(RefreshTokenRequiredException.class)
  public ResponseEntity<ApiResponse<String>> handleRefreshTokenRequiredException(
      RefreshTokenRequiredException e) {
    ApiResponse<String> errorResponse = ApiResponse.error(e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(TokenRequiredException.class)
  public ResponseEntity<ApiResponse<String>> handleTokenRequiredException(
      TokenRequiredException e) {
    ApiResponse<String> errorResponse = ApiResponse.error(e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(AccessDeniedException e) {
    ApiResponse<String> errorResponse = ApiResponse.error(e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<String>> handleAuthenticationException(
      AuthenticationException e) {
    ApiResponse<String> errorResponse = ApiResponse.error(e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<String>> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    ApiResponse<String> errorResponse = ApiResponse.error(ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ApiResponse<String>> handleInvalidTokenException(InvalidTokenException ex) {
    ApiResponse<String> errorResponse = ApiResponse.error(ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(RefreshTokenRevokedException.class)
  public ResponseEntity<ApiResponse<String>> handleRefreshTokenRevokedException(
      RefreshTokenRevokedException ex) {
    ApiResponse<String> errorResponse = ApiResponse.error(ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(BadException.class)
  public ResponseEntity<ApiResponse<String>> handleBadException(BadException ex) {
    ApiResponse<String> errorResponse = ApiResponse.error(ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(FileStorageException.class)
  public ResponseEntity<ApiResponse<String>> handleFileStorageException(FileStorageException ex) {
    ApiResponse<String> errorResponse = ApiResponse.error(ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex) {
    ApiResponse<String> errorResponse = ApiResponse.error(ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(RateLimitException.class)
  public ResponseEntity<ApiResponse<String>> handleRateLimit(RateLimitException ex) {
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .body(ApiResponse.error(ex.getMessage()));
  }
}
