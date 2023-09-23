package com.github.ynovice.felicita.model.dto.entity;

import com.github.ynovice.felicita.model.entity.OrderEntry;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderEntryDto {

    private Long id;
    private ItemDto item;
    private Integer pricePerItem;
    private Integer quantity;

    public static OrderEntryDto fromEntity(OrderEntry orderEntry) {

        OrderEntryDto dto = new OrderEntryDto();
        dto.setId(orderEntry.getId());
        dto.setItem(ItemDto.fromEntity(orderEntry.getItem()));
        dto.setPricePerItem(orderEntry.getPricePerItem());
        dto.setQuantity(orderEntry.getQuantity());

        return dto;
    }
}
