package com.amalitech.tib.exception;

/** Custom exception for resource not found */
public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
