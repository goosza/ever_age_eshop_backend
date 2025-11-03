package com.everage.eshop.exception;

public class InvalidItemStatusException extends RuntimeException {
    public InvalidItemStatusException(String message) {
        super(message);
    }
}