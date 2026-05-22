package com.everage.eshop.service.shipping.zasilkovna;

import com.everage.eshop.entity.ShippingStatus;
import com.everage.eshop.service.shipping.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for processing Zasilkovna webhook events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZasilkovnaWebhookService {

    private final ShippingService shippingService;

    /**
     * Process webhook event from Zasilkovna
     * 
     * @param eventType Event type (e.g., "shipment.created", "shipment.in_transit")
     * @param trackingNumber Tracking number from webhook payload
     */
    @Transactional
    public void processWebhookEvent(String eventType, String trackingNumber) {
        log.info("Processing Zasilkovna webhook: type={}, trackingNumber={}", eventType, trackingNumber);
        
        switch (eventType) {
            case "shipment.created":
                log.info("Shipment created: {}", trackingNumber);
                shippingService.updateShippingStatusByTracking(trackingNumber, ShippingStatus.CREATED);
                // TODO: Send email notification to customer
                break;
                
            case "shipment.in_transit":
                log.info("Shipment in transit: {}", trackingNumber);
                shippingService.updateShippingStatusByTracking(trackingNumber, ShippingStatus.IN_TRANSIT);
                // TODO: Send email notification to customer
                break;
                
            case "shipment.ready_for_pickup":
                log.info("Shipment ready for pickup: {}", trackingNumber);
                shippingService.updateShippingStatusByTracking(trackingNumber, ShippingStatus.READY_FOR_PICKUP);
                // TODO: Send email notification to customer with pickup point details
                break;
                
            case "shipment.delivered":
                log.info("Shipment delivered: {}", trackingNumber);
                shippingService.updateShippingStatusByTracking(trackingNumber, ShippingStatus.DELIVERED);
                // TODO: Update order status to DELIVERED
                // TODO: Send thank you email to customer
                break;
                
            case "shipment.returned":
                log.info("Shipment returned: {}", trackingNumber);
                shippingService.updateShippingStatusByTracking(trackingNumber, ShippingStatus.RETURNED);
                // TODO: Send notification email to customer and admin
                break;
                
            default:
                log.warn("Unknown webhook event type: {}", eventType);
        }
    }
}
