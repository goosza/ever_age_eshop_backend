package com.everage.eshop.dto.mapper;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.entity.Collection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CollectionMapper {
    @Mapping(target = "itemCount", expression = "java(collection.getItems() != null ? collection.getItems().size() : 0)")
    CollectionDto toDto(Collection collection);
    List<CollectionDto> toDtoList(List<Collection> items);
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Collection toEntity(CollectionDto collectionDto);
}
