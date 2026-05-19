package com.everage.eshop.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ZasilkovnaWebhookEvent {
    private String type;
    private ZasilkovnaWebhookData data;
    private LocalDateTime timestamp;
}
