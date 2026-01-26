package com.everage.eshop.dto.mapper;

import com.everage.eshop.dto.PaymentResponse;
import com.everage.eshop.entity.Payment;
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
public interface PaymentMapper {

    @Mapping(target = "orderId", expression = "java(payment.getOrder() != null ? payment.getOrder().getUuid() : null)")
    PaymentResponse toDto(Payment payment);

    List<PaymentResponse> toDtoList(List<Payment> payments);
}
