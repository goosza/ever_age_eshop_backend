package com.everage.eshop.dto.zasilkovna;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShipmentStatusResponse {
    private String trackingNumber;
    private String status;
    private String statusDescription;
    private LocalDateTime lastUpdate;
    private List<StatusHistoryItem> history;

    @Data
    public static class StatusHistoryItem {
        private String status;
        private String description;
        private LocalDateTime timestamp;
        private String location;
    }
}
