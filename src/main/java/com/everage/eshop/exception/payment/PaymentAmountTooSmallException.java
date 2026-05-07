package com.everage.eshop.exception.payment;

public class PaymentAmountTooSmallException extends RuntimeException {
    public PaymentAmountTooSmallException(String message) {
        super(message);
    }
}
