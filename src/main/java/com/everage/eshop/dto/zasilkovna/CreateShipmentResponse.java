package com.everage.eshop.dto.zasilkovna;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateShipmentResponse {
    private String shipmentId;
    private String trackingNumber;
    private String status;
    private String pickupPointId;
    private String pickupPointName;
    private LocalDateTime estimatedDelivery;
    private String labelUrl;
}
