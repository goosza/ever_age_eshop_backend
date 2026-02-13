package com.everage.eshop.dto.mapper;

import com.everage.eshop.dto.CollectionDto;
import com.everage.eshop.entity.Collection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Named("CollectionMapper")
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CollectionMapper {

    /**
     * Maps Collection entity to CollectionDto WITHOUT items.
     * Items will be manually added in the service layer to avoid circular dependency.
     *
     * @param collection entity
     * @return CollectionDto without items
     */
    @Named("toDto")
    @Mapping(target = "items", ignore = true)
    CollectionDto toDto(Collection collection);

    /**
     * Maps list of Collection entities to list of CollectionDto WITHOUT items.
     *
     * @param collections list of entities
     * @return list of CollectionDto without items
     */
    @Named("toDtoList")
    @Mapping(target = "items", ignore = true)
    List<CollectionDto> toDtoList(List<Collection> collections);

    /**
     * Maps Collection entity to CollectionDto WITHOUT items.
     * This method is used when mapping items to avoid circular dependency.
     *
     * @param collection entity
     * @return CollectionDto with items set to null
     */
    @Named("toDtoWithoutItems")
    @Mapping(target = "items", expression = "java(null)")
    CollectionDto toDtoWithoutItems(Collection collection);

    /**
     * Maps CollectionDto to Collection entity.
     *
     * @param collectionDto DTO
     * @return Collection entity
     */
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Collection toEntity(CollectionDto collectionDto);
}