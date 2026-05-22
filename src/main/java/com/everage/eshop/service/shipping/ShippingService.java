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
    private final ShippingGatewayFactory gatewayFactory;

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

        // Integrate with shipping provider using factory pattern
        try {
            ShippingGateway gateway = gatewayFactory.getGateway(request.provider());
            
            if (gateway.isEnabled()) {
                log.info("Creating shipment with provider: {}", request.provider());
                shipping = gateway.createShipment(order, shipping);
            } else {
                log.info("{} integration disabled - using mock tracking number", request.provider());
                shipping.setTrackingNumber(generateFallbackTrackingNumber());
                shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(4));
                shipping.setStatus(ShippingStatus.PENDING);
            }
        } catch (IllegalArgumentException e) {
            // Provider not supported - use fallback
            log.warn("Shipping provider not supported: {} - using fallback", request.provider());
            shipping.setTrackingNumber(generateFallbackTrackingNumber());
            shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(5));
            shipping.setStatus(ShippingStatus.PENDING);
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
            ShippingGateway gateway = gatewayFactory.getGateway(shipping.getProvider());
            return gateway.getShippingLabel(shipping.getTrackingNumber());
        } catch (Exception e) {
            log.error("Failed to fetch shipping label: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch shipping label: " + e.getMessage(), e);
        }
    }

    /**
     * Generate fallback tracking number when API fails
     */
    private String generateFallbackTrackingNumber() {
        return "TRACK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
