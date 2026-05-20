package com.everage.eshop.service.shipping;

import com.everage.eshop.config.ZasilkovnaConfig;
import com.everage.eshop.dto.ShippingRequest;
import com.everage.eshop.dto.ShippingResponse;
import com.everage.eshop.dto.mapper.ShippingMapper;
import com.everage.eshop.dto.zasilkovna.CreateShipmentRequest;
import com.everage.eshop.dto.zasilkovna.CreateShipmentResponse;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.Shipping;
import com.everage.eshop.entity.ShippingProvider;
import com.everage.eshop.entity.ShippingStatus;
import com.everage.eshop.exception.order.OrderNotFoundException;
import com.everage.eshop.repository.OrderRepository;
import com.everage.eshop.repository.ShippingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShippingRepository shippingRepository;
    private final OrderRepository orderRepository;
    private final ShippingMapper shippingMapper;
    private final ZasilkovnaShippingGateway zasilkovnaGateway;
    private final ZasilkovnaConfig zasilkovnaConfig;

    @Value("${zasilkovna.enabled:false}")
    private boolean zasilkovnaEnabled;

    @Transactional
    public ShippingResponse createShipping(ShippingRequest request) {
        log.info("Creating shipping for order: {}", request.orderId());

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + request.orderId()));

        Shipping shipping = new Shipping();
        shipping.setOrder(order);
        shipping.setProvider(request.provider());
        shipping.setAddress(request.address());
        shipping.setStatus(ShippingStatus.PENDING);
        shipping.setCost(request.cost());

        // Save pickup point information
        if (request.pickupPointId() != null) {
            shipping.setPickupPointId(request.pickupPointId());
            shipping.setPickupPointName(request.pickupPointName());
            shipping.setPickupPointAddress(request.pickupPointAddress());
        }

        // Integrate with Zasilkovna API
        if (ShippingProvider.ZASILKOVNA.equals(request.provider())) {
            if (zasilkovnaEnabled) {
                try {
                    log.info("Creating Zasilkovna shipment for order: {}", order.getOrderNumber());
                    
                    // Build Zasilkovna API request
                    CreateShipmentRequest zasilkovnaRequest = 
                        CreateShipmentRequest.builder()
                            .orderId(order.getUuid().toString())
                            .pickupPointId(request.pickupPointId())
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

                } catch (Exception e) {
                    log.error("Failed to create Zasilkovna shipment: {}", e.getMessage(), e);
                    
                    // Fallback: generate tracking number and set status to PENDING for retry
                    shipping.setTrackingNumber(generateFallbackTrackingNumber());
                    shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(4));
                    shipping.setStatus(ShippingStatus.PENDING);
                    
                    log.warn("Using fallback tracking number: {}", shipping.getTrackingNumber());
                }
            } else {
                // Zasilkovna disabled - use mock data for development/testing
                log.info("Zasilkovna integration disabled - using mock tracking number");
                shipping.setTrackingNumber(generateFallbackTrackingNumber());
                shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(4));
                shipping.setStatus(ShippingStatus.PENDING);
                log.info("Mock tracking number generated: {}", shipping.getTrackingNumber());
            }
        } else {
            // For other providers (future implementation)
            shipping.setTrackingNumber(generateFallbackTrackingNumber());
            shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(5));
        }

        shipping = shippingRepository.persist(shipping);
        log.info("Shipping created: {} with tracking: {}", shipping.getUuid(), shipping.getTrackingNumber());

        return shippingMapper.toDto(shipping);
    }

    @Transactional
    public ShippingResponse updateShippingStatus(UUID shippingId, ShippingStatus status) {
        log.info("Updating shipping status: {} -> {}", shippingId, status);

        Shipping shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new RuntimeException("Shipping not found: " + shippingId));

        shipping.setStatus(status);
        shipping = shippingRepository.persist(shipping);

        log.info("Shipping status updated successfully");
        return shippingMapper.toDto(shipping);
    }

    @Transactional
    public ShippingResponse updateShippingStatusByTracking(String trackingNumber, ShippingStatus status) {
        log.info("Updating shipping status by tracking: {} -> {}", trackingNumber, status);

        Shipping shipping = shippingRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipping not found with tracking: " + trackingNumber));

        shipping.setStatus(status);
        shipping = shippingRepository.persist(shipping);

        log.info("Shipping status updated successfully");
        return shippingMapper.toDto(shipping);
    }

    @Transactional(readOnly = true)
    public ShippingResponse getShippingByOrderId(UUID orderUuid) {
        log.info("Fetching shipping for order: {}", orderUuid);

        Shipping shipping = shippingRepository.findByOrderUuid(orderUuid)
                .orElseThrow(() -> new RuntimeException("Shipping not found for order: " + orderUuid));

        return shippingMapper.toDto(shipping);
    }

    @Transactional(readOnly = true)
    public ShippingResponse getShippingByTracking(String trackingNumber) {
        log.info("Fetching shipping by tracking: {}", trackingNumber);

        Shipping shipping = shippingRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipping not found with tracking: " + trackingNumber));

        return shippingMapper.toDto(shipping);
    }

    @Transactional(readOnly = true)
    public byte[] getShippingLabel(UUID orderUuid) {
        log.info("Fetching shipping label for order: {}", orderUuid);

        Shipping shipping = shippingRepository.findByOrderUuid(orderUuid)
                .orElseThrow(() -> new RuntimeException("Shipping not found for order: " + orderUuid));

        if (shipping.getTrackingNumber() == null) {
            throw new RuntimeException("No tracking number available for shipping");
        }

        try {
            return zasilkovnaGateway.getShippingLabel(shipping.getTrackingNumber());
        } catch (Exception e) {
            log.error("Failed to fetch shipping label: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch shipping label: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate parcel weight based on order items
     * Sums up weight of all items considering their quantities
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
