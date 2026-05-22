package com.everage.eshop.service.shipping.zasilkovna;

import com.everage.eshop.config.ZasilkovnaConfig;
import com.everage.eshop.dto.zasilkovna.CreateShipmentRequest;
import com.everage.eshop.dto.zasilkovna.CreateShipmentResponse;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.Shipping;
import com.everage.eshop.entity.ShippingProvider;
import com.everage.eshop.entity.ShippingStatus;
import com.everage.eshop.service.shipping.ShippingGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Adapter for Zasilkovna shipping provider
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ZasilkovnaGatewayAdapter implements ShippingGateway {

    private final ZasilkovnaShippingGateway zasilkovnaGateway;
    private final ZasilkovnaConfig zasilkovnaConfig;

    @Value("${zasilkovna.enabled:false}")
    private boolean zasilkovnaEnabled;

    @Override
    public ShippingProvider getProvider() {
        return ShippingProvider.ZASILKOVNA;
    }

    @Override
    public boolean isEnabled() {
        return zasilkovnaEnabled;
    }

    @Override
    public Shipping createShipment(Order order, Shipping shipping) {
        if (!zasilkovnaEnabled) {
            log.info("Zasilkovna integration disabled - using mock tracking number");
            shipping.setTrackingNumber(generateFallbackTrackingNumber());
            shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(4));
            shipping.setStatus(ShippingStatus.PENDING);
            return shipping;
        }

        try {
            log.info("Creating Zasilkovna shipment for order: {}", order.getOrderNumber());

            // Build Zasilkovna API request
            CreateShipmentRequest zasilkovnaRequest =
                    CreateShipmentRequest.builder()
                            .orderId(order.getUuid().toString())
                            .pickupPointId(shipping.getPickupPointId())
                            .recipient(CreateShipmentRequest.Recipient.builder()
                                    .name(order.getFirstName() + " " + order.getLastName())
                                    .email(order.getEmail())
                                    .phone(order.getPhone())
                                    .build())
                            .sender(CreateShipmentRequest.Sender.builder()
                                    .id(zasilkovnaConfig.getSender().getId())
                                    .name(zasilkovnaConfig.getSender().getName())
                                    .email(zasilkovnaConfig.getSender().getEmail())
                                    .phone(zasilkovnaConfig.getSender().getPhone())
                                    .build())
                            .parcel(CreateShipmentRequest.Parcel.builder()
                                    .weight(calculateWeight(order))
                                    .value(order.getTotalAmount().doubleValue())
                                    .dimensions(CreateShipmentRequest.Parcel.Dimensions.builder()
                                            .length(30)
                                            .width(20)
                                            .height(10)
                                            .build())
                                    .build())
                            .payment(CreateShipmentRequest.Payment.builder()
                                    .method("prepaid")  // Already paid via Stripe
                                    .amount(0.0)
                                    .build())
                            .build();

            // Call Zasilkovna API
            CreateShipmentResponse zasilkovnaResponse =
                    zasilkovnaGateway.createShipment(zasilkovnaRequest);

            // Save Zasilkovna response data
            shipping.setTrackingNumber(zasilkovnaResponse.getTrackingNumber());
            shipping.setShipmentId(zasilkovnaResponse.getShipmentId());
            shipping.setLabelUrl(zasilkovnaResponse.getLabelUrl());
            shipping.setEstimatedDelivery(zasilkovnaResponse.getEstimatedDelivery());
            shipping.setStatus(ShippingStatus.CREATED);

            log.info("Zasilkovna shipment created successfully: {}", zasilkovnaResponse.getTrackingNumber());
            return shipping;

        } catch (Exception e) {
            log.error("Failed to create Zasilkovna shipment: {}", e.getMessage(), e);

            // Fallback: generate tracking number and set status to PENDING for retry
            shipping.setTrackingNumber(generateFallbackTrackingNumber());
            shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(4));
            shipping.setStatus(ShippingStatus.PENDING);

            log.warn("Using fallback tracking number: {}", shipping.getTrackingNumber());
            return shipping;
        }
    }

    @Override
    public byte[] getShippingLabel(String trackingNumber) {
        return zasilkovnaGateway.getShippingLabel(trackingNumber);
    }

    @Override
    public void cancelShipment(String trackingNumber) {
        zasilkovnaGateway.cancelShipment(trackingNumber);
    }

    /**
     * Calculate parcel weight based on order items
     */
    private Double calculateWeight(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return 0.5; // Default minimum weight
        }

        double totalWeight = order.getItems().stream()
                .mapToDouble(orderItem -> {
                    BigDecimal itemWeight = orderItem.getItem().getWeight();
                    int quantity = orderItem.getQuantity();
                    return itemWeight.doubleValue() * quantity;
                })
                .sum();

        // Ensure minimum weight of 0.1 kg
        return Math.max(0.1, totalWeight);
    }

    /**
     * Generate fallback tracking number when API fails
     */
    private String generateFallbackTrackingNumber() {
        return "TRACK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
