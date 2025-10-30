package com.amalitech.tib.exception;

public class RateLimitException extends RuntimeException {
  public RateLimitException(String message) {
    super(message);
  }
}
