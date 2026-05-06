package com.everage.eshop.service.shipping;

import com.everage.eshop.config.ZasilkovnaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZasilkovnaShippingGateway {

    private final ZasilkovnaConfig zasilkovnaConfig;
    private final WebClient zasilkovnaWebClient;

    /**
     * Create shipment in Zasilkovna system
     */
    public ZasilkovnaShipmentResponse createShipment(ZasilkovnaShipmentRequest request) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("recipient_name", request.getRecipientName());
            requestBody.put("recipient_phone", request.getRecipientPhone());
            requestBody.put("recipient_email", request.getRecipientEmail());
            requestBody.put("address", request.getAddress());
            requestBody.put("parcel_id", request.getParcelId());
            requestBody.put("parcel_weight", request.getParcelWeight());
            requestBody.put("parcel_value", request.getParcelValue());

            ZasilkovnaShipmentResponse response = zasilkovnaWebClient.post()
                    .uri("/shipment/create")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(ZasilkovnaShipmentResponse.class)
                    .block();

            log.info("Shipment created in Zasilkovna: {}", response.getTrackingNumber());
            return response;

        } catch (WebClientResponseException e) {
            log.error("Zasilkovna API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Zasilkovna shipment creation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to create Zasilkovna shipment: {}", e.getMessage());
            throw new RuntimeException("Shipment creation error: " + e.getMessage(), e);
        }
    }

    /**
     * Get parcel locker locations
     */
    public ZasilkovnaLocationResponse getLocations(String city, String country) {
        try {
            ZasilkovnaLocationResponse response = zasilkovnaWebClient.get()
                    .uri("/locations?city=" + city + "&country=" + country)
                    .retrieve()
                    .bodyToMono(ZasilkovnaLocationResponse.class)
                    .block();

            log.info("Retrieved {} Zasilkovna locations for {}, {}",
                    response.getLocations().size(), city, country);
            return response;

        } catch (Exception e) {
            log.error("Failed to get Zasilkovna locations: {}", e.getMessage());
            throw new RuntimeException("Location retrieval error: " + e.getMessage(), e);
        }
    }

    /**
     * Track shipment
     */
    public ZasilkovnaTrackingResponse trackShipment(String trackingNumber) {
        try {
            ZasilkovnaTrackingResponse response = zasilkovnaWebClient.get()
                    .uri("/tracking/" + trackingNumber)
                    .retrieve()
                    .bodyToMono(ZasilkovnaTrackingResponse.class)
                    .block();

            log.info("Tracking info retrieved for: {}", trackingNumber);
            return response;

        } catch (Exception e) {
            log.error("Failed to track shipment: {}", e.getMessage());
            throw new RuntimeException("Tracking error: " + e.getMessage(), e);
        }
    }

    // DTOs for Zasilkovna API

    public static class ZasilkovnaShipmentRequest {
        private String recipientName;
        private String recipientPhone;
        private String recipientEmail;
        private String address;
        private String parcelId;
        private Double parcelWeight;
        private Double parcelValue;

        // Getters and setters
        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

        public String getRecipientPhone() { return recipientPhone; }
        public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

        public String getRecipientEmail() { return recipientEmail; }
        public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getParcelId() { return parcelId; }
        public void setParcelId(String parcelId) { this.parcelId = parcelId; }

        public Double getParcelWeight() { return parcelWeight; }
        public void setParcelWeight(Double parcelWeight) { this.parcelWeight = parcelWeight; }

        public Double getParcelValue() { return parcelValue; }
        public void setParcelValue(Double parcelValue) { this.parcelValue = parcelValue; }
    }

    public static class ZasilkovnaShipmentResponse {
        private String trackingNumber;
        private String shipmentId;
        private String status;

        public ZasilkovnaShipmentResponse() {}

        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

        public String getShipmentId() { return shipmentId; }
        public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class ZasilkovnaLocationResponse {
        private java.util.List<ZasilkovnaLocation> locations;

        public java.util.List<ZasilkovnaLocation> getLocations() { return locations; }
        public void setLocations(java.util.List<ZasilkovnaLocation> locations) { this.locations = locations; }
    }

    public static class ZasilkovnaLocation {
        private String id;
        private String name;
        private String address;
        private String city;
        private String postalCode;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    }

    public static class ZasilkovnaTrackingResponse {
        private String trackingNumber;
        private String status;
        private String lastUpdate;
        private String estimatedDelivery;

        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(String lastUpdate) { this.lastUpdate = lastUpdate; }

        public String getEstimatedDelivery() { return estimatedDelivery; }
        public void setEstimatedDelivery(String estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    }
}
