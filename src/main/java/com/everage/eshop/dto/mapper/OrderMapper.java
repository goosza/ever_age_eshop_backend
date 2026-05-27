package com.everage.eshop.dto.mapper;

import com.everage.eshop.dto.OrderDto;
import com.everage.eshop.dto.OrderItemDto;
import com.everage.eshop.dto.OrderPaymentDto;
import com.everage.eshop.dto.OrderShippingDto;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.OrderItem;
import com.everage.eshop.entity.Payment;
import com.everage.eshop.entity.Shipping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderMapper {

    @Mapping(target = "shipping", expression = "java(toShippingInfo(order.getShipping()))")
    @Mapping(target = "payment", expression = "java(toPaymentInfo(order.getPayment()))")
    OrderDto toDto(Order order);

    List<OrderDto> toDtoList(List<Order> orders);

    @Mapping(target = "itemUuid", expression = "java(orderItem.getItem() != null ? orderItem.getItem().getUuid() : null)")
    OrderItemDto toItemDto(OrderItem orderItem);

    List<OrderItemDto> toItemDtoList(List<OrderItem> orderItems);

    default OrderShippingDto toShippingInfo(Shipping shipping) {
        if (shipping == null) return null;
        return new OrderShippingDto(
                shipping.getUuid(),
                shipping.getProvider(),
                shipping.getStatus(),
                shipping.getTrackingNumber(),
                shipping.getEstimatedDelivery(),
                shipping.getAddress(),
                shipping.getCity(),
                shipping.getPostalCode(),
                shipping.getCountry(),
                shipping.getPickupPointId(),
                shipping.getPickupPointName(),
                shipping.getPickupPointAddress()
        );
    }

    default OrderPaymentDto toPaymentInfo(Payment payment) {
        if (payment == null) return null;
        return new OrderPaymentDto(
                payment.getUuid(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getAmount()
        );
    }
}
