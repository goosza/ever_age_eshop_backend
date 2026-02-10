package com.everage.eshop.dto.mapper;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CollectionMapper.class})
public interface ItemMapper {
    @Mapping(target = "collection", source = "collection")
    ItemDto toDto(Item item);

    List<ItemDto> toDtoList(List<Item> items);
    @Mapping(target = "collection", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    Item toEntity(ItemDto itemDto);
}
