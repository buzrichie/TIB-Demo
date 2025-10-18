package com.amalitech.tib.shared.exception;

public class RefreshTokenRequiredException extends RuntimeException {
    public RefreshTokenRequiredException(String message) {
        super(message);
    }
}
