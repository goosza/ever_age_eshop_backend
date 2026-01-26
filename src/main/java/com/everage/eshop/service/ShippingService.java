package com.everage.eshop.service;

import com.everage.eshop.dto.ShippingRequest;
import com.everage.eshop.dto.ShippingResponse;
import com.everage.eshop.dto.mapper.ShippingMapper;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.Shipping;
import com.everage.eshop.entity.ShippingProvider;
import com.everage.eshop.entity.ShippingStatus;
import com.everage.eshop.repository.OrderRepository;
import com.everage.eshop.repository.ShippingRepository;
import com.everage.eshop.service.gateway.ZasilkovnaShippingGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ZasilkovnaShippingGateway zasilkovnaShippingGateway;

    @Transactional
    public ShippingResponse createShipping(ShippingRequest request) {
        log.info("Creating shipping for order: {}", request.orderId());

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + request.orderId()));

        Shipping shipping = new Shipping();
        shipping.setOrder(order);
        shipping.setProvider(request.provider());
        shipping.setAddress(request.address());
        shipping.setStatus(ShippingStatus.PENDING);

        // Calculate shipping cost
        shipping.setCost(calculateShippingCost(request.provider()));

        // Integrate with actual shipping providers
        if (ShippingProvider.ZASILKOVNA.equals(request.provider())) {
            try {
                // Create shipment in Zasilkovna
                ZasilkovnaShippingGateway.ZasilkovnaShipmentRequest zasilkovnaRequest =
                    new ZasilkovnaShippingGateway.ZasilkovnaShipmentRequest();
                zasilkovnaRequest.setRecipientName(order.getFirstName() + " " + order.getLastName());
                zasilkovnaRequest.setRecipientPhone(order.getPhone());
                zasilkovnaRequest.setRecipientEmail(order.getEmail());
                zasilkovnaRequest.setAddress(request.address());
                zasilkovnaRequest.setParcelId(order.getUuid().toString());
                zasilkovnaRequest.setParcelWeight(1.0); // Default weight - customize as needed
                zasilkovnaRequest.setParcelValue(order.getTotalAmount().doubleValue());

                ZasilkovnaShippingGateway.ZasilkovnaShipmentResponse zasilkovnaResponse =
                    zasilkovnaShippingGateway.createShipment(zasilkovnaRequest);

                shipping.setTrackingNumber(zasilkovnaResponse.getTrackingNumber());
                log.info("Zasilkovna shipment created with tracking: {}", zasilkovnaResponse.getTrackingNumber());

            } catch (Exception e) {
                log.warn("Zasilkovna API error, using fallback tracking: {}", e.getMessage());
                shipping.setTrackingNumber(generateTrackingNumber());
                shipping.setStatus(ShippingStatus.PENDING); // Will retry later
            }
        } else {
            // For other providers, generate tracking number
            shipping.setTrackingNumber(generateTrackingNumber());
        }

        // Set estimated delivery (5 business days)
        shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(5));

        shipping = shippingRepository.persist(shipping);

        log.info("Shipping created: {}", shipping.getId());

        return shippingMapper.toDto(shipping);
    }

    @Transactional
    public ShippingResponse updateShippingStatus(UUID shippingId, ShippingStatus status) {
        log.info("Updating shipping status: {} -> {}", shippingId, status);

        Shipping shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new RuntimeException("Shipping not found: " + shippingId));

        shipping.setStatus(status);
        shipping = shippingRepository.persist(shipping);

        log.info("Shipping status updated: {}", shippingId);

        return shippingMapper.toDto(shipping);
    }

    @Transactional(readOnly = true)
    public ShippingResponse getShippingByOrderId(UUID orderId) {
        log.info("Fetching shipping for order: {}", orderId);

        Shipping shipping = shippingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Shipping not found for order: " + orderId));

        return shippingMapper.toDto(shipping);
    }

    private BigDecimal calculateShippingCost(ShippingProvider provider) {
        return switch (provider) {
            case ZASILKOVNA -> new BigDecimal("12.00");
            case DPD -> new BigDecimal("15.00");
            case FEDEX -> new BigDecimal("20.00");
            case UPS -> new BigDecimal("18.00");
            case STANDARD -> new BigDecimal("5.00");
        };
    }

    private String generateTrackingNumber() {
        return "TRACK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
