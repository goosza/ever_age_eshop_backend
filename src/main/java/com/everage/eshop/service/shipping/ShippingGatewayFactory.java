package com.everage.eshop.service.shipping;

import com.everage.eshop.entity.ShippingProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for getting the appropriate shipping gateway based on provider
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingGatewayFactory {

    private final List<ShippingGateway> shippingGateways;
    private Map<ShippingProvider, ShippingGateway> gatewayMap;

    /**
     * Get shipping gateway for the specified provider
     * 
     * @param provider Shipping provider
     * @return ShippingGateway implementation
     * @throws IllegalArgumentException if provider not supported
     */
    public ShippingGateway getGateway(ShippingProvider provider) {
        if (gatewayMap == null) {
            // Lazy initialization - build map from all available gateways
            gatewayMap = shippingGateways.stream()
                    .collect(Collectors.toMap(
                            ShippingGateway::getProvider,
                            Function.identity()
                    ));
            
            log.info("Initialized shipping gateways: {}", gatewayMap.keySet());
        }

        ShippingGateway gateway = gatewayMap.get(provider);
        
        if (gateway == null) {
            throw new IllegalArgumentException(
                    "Shipping provider not supported: " + provider + 
                    ". Available providers: " + gatewayMap.keySet()
            );
        }

        return gateway;
    }

    /**
     * Check if provider is supported
     */
    public boolean isSupported(ShippingProvider provider) {
        try {
            getGateway(provider);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get all supported providers
     */
    public List<ShippingProvider> getSupportedProviders() {
        if (gatewayMap == null) {
            getGateway(ShippingProvider.ZASILKOVNA); // Initialize
        }
        return List.copyOf(gatewayMap.keySet());
    }
}
