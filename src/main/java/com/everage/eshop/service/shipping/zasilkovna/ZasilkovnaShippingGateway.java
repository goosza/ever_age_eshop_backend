package com.everage.eshop.service.shipping.zasilkovna;

import com.everage.eshop.config.ZasilkovnaConfig;
import com.everage.eshop.dto.zasilkovna.CreateShipmentRequest;
import com.everage.eshop.dto.zasilkovna.CreateShipmentResponse;
import com.everage.eshop.dto.zasilkovna.ShipmentStatusResponse;
import com.everage.eshop.exception.shipping.ZasilkovnaApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZasilkovnaShippingGateway {

    private final ZasilkovnaConfig zasilkovnaConfig;
    private final WebClient zasilkovnaWebClient;

    /**
     * Create shipment in Zasilkovna system
     */
    public CreateShipmentResponse createShipment(CreateShipmentRequest request) {
        try {
            log.info("Creating Zasilkovna shipment for order: {}", request.getOrderId());
            
            CreateShipmentResponse response = zasilkovnaWebClient.post()
                    .uri("/shipments")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CreateShipmentResponse.class)
                    .block();

            log.info("Zasilkovna shipment created successfully: {}", response.getTrackingNumber());
            return response;

        } catch (WebClientResponseException e) {
            log.error("Zasilkovna API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ZasilkovnaApiException("Failed to create shipment: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating Zasilkovna shipment: {}", e.getMessage(), e);
            throw new ZasilkovnaApiException("Shipment creation error: " + e.getMessage(), e);
        }
    }

    /**
     * Get shipment status and tracking information
     */
    public ShipmentStatusResponse getShipmentStatus(String trackingNumber) {
        try {
            log.info("Fetching Zasilkovna shipment status: {}", trackingNumber);
            
            ShipmentStatusResponse response = zasilkovnaWebClient.get()
                    .uri("/shipments/{trackingNumber}/status", trackingNumber)
                    .retrieve()
                    .bodyToMono(ShipmentStatusResponse.class)
                    .block();

            log.info("Shipment status retrieved: {} - {}", trackingNumber, response.getStatus());
            return response;

        } catch (WebClientResponseException e) {
            log.error("Failed to get shipment status: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ZasilkovnaApiException("Failed to get shipment status: " + e.getMessage(), e);
        }
    }

    /**
     * Get shipping label PDF
     */
    public byte[] getShippingLabel(String trackingNumber) {
        try {
            log.info("Fetching Zasilkovna shipping label: {}", trackingNumber);
            
            byte[] labelPdf = zasilkovnaWebClient.get()
                    .uri("/shipments/{trackingNumber}/label", trackingNumber)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            log.info("Shipping label retrieved successfully: {} bytes", labelPdf != null ? labelPdf.length : 0);
            return labelPdf;

        } catch (WebClientResponseException e) {
            log.error("Failed to get shipping label: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ZasilkovnaApiException("Failed to get shipping label: " + e.getMessage(), e);
        }
    }

    /**
     * Cancel shipment
     */
    public void cancelShipment(String trackingNumber) {
        try {
            log.info("Cancelling Zasilkovna shipment: {}", trackingNumber);
            
            zasilkovnaWebClient.delete()
                    .uri("/shipments/{trackingNumber}", trackingNumber)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Shipment cancelled successfully: {}", trackingNumber);

        } catch (WebClientResponseException e) {
            log.error("Failed to cancel shipment: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ZasilkovnaApiException("Failed to cancel shipment: " + e.getMessage(), e);
        }
    }
}
