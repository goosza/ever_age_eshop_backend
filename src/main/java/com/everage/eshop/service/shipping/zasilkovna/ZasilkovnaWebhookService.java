package com.everage.eshop.service.shipping.zasilkovna;

import com.everage.eshop.entity.Shipping;
import com.everage.eshop.entity.ShippingStatus;
import com.everage.eshop.repository.ShippingRepository;
import com.everage.eshop.service.EmailService;
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
    private final ShippingRepository shippingRepository;
    private final EmailService emailService;

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
                stubNotify("shipment.created", trackingNumber, "customer notification (no template yet)");
                break;
                
            case "shipment.in_transit":
                log.info("Shipment in transit: {}", trackingNumber);
                shippingService.updateShippingStatusByTracking(trackingNumber, ShippingStatus.IN_TRANSIT);
                notifyShipped(trackingNumber);
                break;
                
            case "shipment.ready_for_pickup":
                log.info("Shipment ready for pickup: {}", trackingNumber);
                shippingService.updateShippingStatusByTracking(trackingNumber, ShippingStatus.READY_FOR_PICKUP);
                stubNotify("shipment.ready_for_pickup", trackingNumber,
                        "customer notification with pickup point details (no template yet)");
                break;
                
            case "shipment.delivered":
                log.info("Shipment delivered: {}", trackingNumber);
                shippingService.updateShippingStatusByTracking(trackingNumber, ShippingStatus.DELIVERED);
                stubNotify("shipment.delivered", trackingNumber,
                        "order status update to DELIVERED + thank-you email (not implemented yet)");
                break;
                
            case "shipment.returned":
                log.info("Shipment returned: {}", trackingNumber);
                shippingService.updateShippingStatusByTracking(trackingNumber, ShippingStatus.RETURNED);
                stubNotify("shipment.returned", trackingNumber,
                        "customer + admin notification (no template yet)");
                break;
                
            default:
                log.warn("Unknown webhook event type: {}", eventType);
        }
    }

    /**
     * Looks up the shipping/order pair for the given tracking number and sends the
     * "your order is on its way" email. Failures are logged but never propagated —
     * a webhook from Zasilkovna should not fail just because the notification email
     * couldn't be sent (EmailService itself also swallows send failures).
     */
    private void notifyShipped(String trackingNumber) {
        shippingRepository.findByTrackingNumber(trackingNumber).ifPresentOrElse(
                shipping -> emailService.sendShippingNotification(shipping.getOrder(), shipping),
                () -> log.warn("Cannot send shipping notification — no shipping found for tracking: {}", trackingNumber)
        );
    }

    /**
     * No-op placeholder for webhook events that don't have a real notification
     * implementation yet (missing email template/EmailService method). Logs
     * clearly as a STUB so it's easy to find and doesn't get mistaken for a
     * silently-dropped feature.
     */
    private void stubNotify(String eventType, String trackingNumber, String whatWouldHappen) {
        log.warn("STUB: no-op for '{}' (tracking: {}) — would send: {}", eventType, trackingNumber, whatWouldHappen);
    }
}
