package com.everage.eshop.exception;

import com.everage.eshop.exception.collection.CollectionAlreadyExistsException;
import com.everage.eshop.exception.collection.CollectionNotFoundException;
import com.everage.eshop.exception.item.InsufficientStockException;
import com.everage.eshop.exception.item.InvalidItemStatusException;
import com.everage.eshop.exception.item.ItemAlreadyExistsException;
import com.everage.eshop.exception.item.ItemNotFoundException;
import com.everage.eshop.exception.item.ItemNotInCollectionException;
import com.everage.eshop.exception.order.OrderNotFoundException;
import com.everage.eshop.exception.payment.PaymentAmountTooSmallException;
import com.everage.eshop.exception.payment.PaymentGatewayException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleItemNotFound(ItemNotFoundException ex) {
        log.error("Item not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ItemAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleItemAlreadyExists(ItemAlreadyExistsException ex) {
        log.error("Item already exists: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        log.error("Insufficient stock: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidItemStatusException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidItemStatus(InvalidItemStatusException ex) {
        log.error("Invalid item status: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ============================================
    // Collection Exceptions
    // ============================================

    @ExceptionHandler(CollectionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCollectionNotFound(CollectionNotFoundException ex) {
        log.error("Collection not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CollectionAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleCollectionAlreadyExists(CollectionAlreadyExistsException ex) {
        log.error("Collection already exists: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ItemNotInCollectionException.class)
    public ResponseEntity<Map<String, Object>> handleItemNotInCollection(ItemNotInCollectionException ex) {
        log.error("Item not in collection: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ============================================
    // Payment Exceptions
    // ============================================

    @ExceptionHandler(PaymentAmountTooSmallException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentAmountTooSmall(PaymentAmountTooSmallException ex) {
        log.error("Payment amount too small: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentGateway(PaymentGatewayException ex) {
        log.error("Payment gateway error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ============================================
    // General Exceptions
    // ============================================

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOrderNotFound(OrderNotFoundException ex) {
        log.error("Order not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unexpected error: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // ============================================
    // Helper method
    // ============================================

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", status.value(),
                        "error", status.getReasonPhrase(),
                        "message", message
                ));
    }
}