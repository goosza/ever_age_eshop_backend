package com.everage.eshop.exception.shipping;

public class ZasilkovnaApiException extends RuntimeException {
    public ZasilkovnaApiException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ZasilkovnaApiException(String message) {
        super(message);
    }
}
