package com.lorenzodm.librepm.api.exception;

public class OwnershipViolationException extends RuntimeException {
    public OwnershipViolationException(String message) {
        super(message);
    }
}
