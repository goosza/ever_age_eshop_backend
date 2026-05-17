package com.everage.eshop.dto;

import lombok.Data;

@Data
public class ZasilkovnaWebhookData {
    private String trackingNumber;
    private String shipmentId;
    private String status;
    private String statusDescription;
    private String pickupPointId;
    private String pickupPointName;
}
