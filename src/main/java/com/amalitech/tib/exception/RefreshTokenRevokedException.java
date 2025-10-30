package com.amalitech.tib.exception;

public class RefreshTokenRevokedException extends RuntimeException {
  public RefreshTokenRevokedException(String message) {
    super(message);
  }
}
