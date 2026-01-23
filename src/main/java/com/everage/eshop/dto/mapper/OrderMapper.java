package com.everage.eshop.dto.mapper;

import com.everage.eshop.dto.OrderDTO;
import com.everage.eshop.dto.OrderItemDTO;
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

    OrderDTO toDto(Order order);

    List<OrderDTO> toDtoList(List<Order> orders);

    @Mapping(target = "itemUuid", expression = "java(orderItem.getItem() != null ? orderItem.getItem().getUuid() : null)")
    OrderItemDTO toItemDto(OrderItem orderItem);

    List<OrderItemDTO> toItemDtoList(List<OrderItem> orderItems);
}