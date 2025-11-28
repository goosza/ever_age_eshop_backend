package com.everage.eshop.dto;

import com.everage.eshop.entity.Order;
import com.everage.eshop.entity.OrderItem;
import com.everage.eshop.entity.OrderStatus;
import com.everage.eshop.entity.OrderStatusHistory;
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
    @Mapping(target = "statusDisplay", expression = "java(order.getStatus().getDisplayName())")
    @Mapping(target = "itemId", expression = "java(orderItem.getItem() != null ? orderItem.getItem().getUuid() : null)")
    OrderDTO toDto(Order order);

    List<OrderDTO> toDtoList(List<Order> orders);

    @Mapping(target = "itemId", expression = "java(orderItem.getItem() != null ? orderItem.getItem().getUuid() : null)")
    OrderItemDTO toItemDto(OrderItem orderItem);

    List<OrderItemDTO> toItemDtoList(List<OrderItem> orderItems);

    @Mapping(target = "oldStatusDisplay", expression = "java(getStatusDisplay(history.getOldStatus()))")
    @Mapping(target = "newStatusDisplay", expression = "java(getStatusDisplay(history.getNewStatus()))")
    OrderStatusHistoryDTO toHistoryDto(OrderStatusHistory history);

    List<OrderStatusHistoryDTO> toHistoryDtoList(List<OrderStatusHistory> histories);

    default String getStatusDisplay(OrderStatus status) {
        return status != null ? status.getDisplayName() : null;
    }
}