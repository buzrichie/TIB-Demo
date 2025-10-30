package com.amalitech.tib.exception;

public class RefreshTokenRequiredException extends RuntimeException {
  public RefreshTokenRequiredException(String message) {
    super(message);
  }
}
