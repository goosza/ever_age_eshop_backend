package com.everage.eshop.dto.mapper;

import com.everage.eshop.dto.ItemDto;
import com.everage.eshop.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Named("ItemMapper")
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CollectionMapper.class})
public interface ItemMapper {

    // Full DTO with collection (collection without items)
    @Named("toDto")
    @Mapping(target = "collection", qualifiedByName = {"CollectionMapper", "toDtoWithoutItems"})
    ItemDto toDto(Item item);

    // Items list with collection (collection without items)
    @Named("toDtoList")
    @Mapping(target = "collection", qualifiedByName = {"CollectionMapper", "toDtoWithoutItems"})
    List<ItemDto> toDtoList(List<Item> items);

    // DTO without collection (used in CollectionDto.items)
    @Named("toDtoWithoutCollection")
    @Mapping(target = "collection", expression = "java(null)")
    ItemDto toDtoWithoutCollection(Item item);

    // Items list without collection (used in CollectionDto.items)
    @Named("toDtoListWithoutCollection")
    @Mapping(target = "collection", expression = "java(null)")
    List<ItemDto> toDtoListWithoutCollection(List<Item> items);

    @Mapping(target = "collection", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    Item toEntity(ItemDto itemDto);
}