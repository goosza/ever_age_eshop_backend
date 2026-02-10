package com.everage.eshop.exception.item;

public class ItemNotInCollectionException extends RuntimeException{
    public ItemNotInCollectionException(String message) {
        super(message);
    }
}
