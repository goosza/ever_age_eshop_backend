package com.everage.eshop.dto.mapper;

import com.everage.eshop.dto.OrderDto;
import com.everage.eshop.dto.OrderItemDto;
import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.OrderItem;
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

    OrderDto toDto(Order order);

    List<OrderDto> toDtoList(List<Order> orders);

    @Mapping(target = "itemUuid", expression = "java(orderItem.getItem() != null ? orderItem.getItem().getUuid() : null)")
    OrderItemDto toItemDto(OrderItem orderItem);

    List<OrderItemDto> toItemDtoList(List<OrderItem> orderItems);
}