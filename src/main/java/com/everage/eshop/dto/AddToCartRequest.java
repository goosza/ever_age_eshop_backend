package com.everage.eshop.dto;

import java.util.UUID;

public record AddToCartRequest(UUID itemUuid,
                               Integer quantity) {
}