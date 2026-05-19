package com.everage.eshop.dto.zasilkovna;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateShipmentRequest {
    private String orderId;
    private String pickupPointId;
    private Recipient recipient;
    private Sender sender;
    private Parcel parcel;
    private Payment payment;

    @Data
    @Builder
    public static class Recipient {
        private String name;
        private String email;
        private String phone;
    }

    @Data
    @Builder
    public static class Sender {
        private String id;
        private String name;
        private String email;
        private String phone;
    }

    @Data
    @Builder
    public static class Parcel {
        private Double weight;        // в кг
        private Double value;         // стоимость в EUR
        private Dimensions dimensions;

        @Data
        @Builder
        public static class Dimensions {
            private Integer length;   // в см
            private Integer width;
            private Integer height;
        }
    }

    @Data
    @Builder
    public static class Payment {
        private String method;        // "card", "cash", "prepaid"
        private Double amount;        // сумма к оплате при получении (COD)
    }
}
