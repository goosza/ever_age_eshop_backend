package com.everage.eshop.exception.item;

public class InvalidItemStatusException extends RuntimeException {
    public InvalidItemStatusException(String message) {
        super(message);
    }
}