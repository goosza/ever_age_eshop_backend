package com.everage.eshop.dto.mapper;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.entity.Item;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto toDto(Item item);
    List<ItemDto> toDtoList(List<Item> items);
    Item toEntity(ItemDto itemDto);
}
