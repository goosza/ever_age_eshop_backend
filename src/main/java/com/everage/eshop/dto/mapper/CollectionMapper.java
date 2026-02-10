package com.everage.eshop.dto.mapper;

import com.everage.eshop.dto.CollectionDetailDto;
import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.entity.Collection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface CollectionMapper {

    CollectionDto toDto(Collection collection);
    List<CollectionDto> toDtoList(List<Collection> items);

    @Mapping(target = "items", source = "items")
    CollectionDetailDto toDetailDto(Collection collection);

    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Collection toEntity(CollectionDto collectionDto);
}
