package com.siemens.internship.mapper;

import com.siemens.internship.dto.ItemDto;
import com.siemens.internship.model.Item;

public class ItemMapper {
    public static ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .description(item.getDescription())
                .email(item.getEmail())
                .name(item.getName())
                .status(item.getStatus())
                .build();
    }

    public static Item toEntity(ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .description(itemDto.getDescription())
                .email(itemDto.getEmail())
                .name(itemDto.getName())
                .status(itemDto.getStatus())
                .build();
    }
}
