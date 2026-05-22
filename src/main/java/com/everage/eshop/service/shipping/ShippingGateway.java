package com.everage.eshop.service.shipping;

import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.Shipping;
import com.everage.eshop.entity.ShippingProvider;

/**
 * Interface for shipping provider integrations.
 * Implement this interface for each shipping provider (Zasilkovna, DHL, FedEx, etc.)
 */
public interface ShippingGateway {
    
    /**
     * Get the provider this gateway handles
     */
    ShippingProvider getProvider();
    
    /**
     * Check if this gateway is enabled
     */
    boolean isEnabled();
    
    /**
     * Create shipment with the provider
     * 
     * @param order Order details
     * @param shipping Shipping entity to populate
     * @return Updated shipping entity with tracking number, etc.
     */
    Shipping createShipment(Order order, Shipping shipping);
    
    /**
     * Get shipping label (PDF)
     * 
     * @param trackingNumber Tracking number
     * @return PDF bytes
     */
    byte[] getShippingLabel(String trackingNumber);
    
    /**
     * Cancel shipment
     * 
     * @param trackingNumber Tracking number
     */
    void cancelShipment(String trackingNumber);
}
