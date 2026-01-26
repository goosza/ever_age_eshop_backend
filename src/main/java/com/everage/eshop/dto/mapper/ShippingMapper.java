package com.everage.eshop.dto.mapper;

import com.everage.eshop.dto.ShippingResponse;
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
public interface ShippingMapper {

    @Mapping(target = "orderId", expression = "java(shipping.getOrder() != null ? shipping.getOrder().getUuid() : null)")
    ShippingResponse toDto(Shipping shipping);

    List<ShippingResponse> toDtoList(List<Shipping> shippings);
}
