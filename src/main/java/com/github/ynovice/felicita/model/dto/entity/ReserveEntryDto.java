package com.github.ynovice.felicita.model.dto.entity;

import com.github.ynovice.felicita.model.entity.ReserveEntry;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReserveEntryDto {

    private Long id;
    private ItemDto item;
    private Integer pricePerItem;
    private Integer quantity;

    public static ReserveEntryDto fromEntity(ReserveEntry reserveEntry) {

        ReserveEntryDto dto = new ReserveEntryDto();
        dto.setId(reserveEntry.getId());
        dto.setItem(ItemDto.fromEntity(reserveEntry.getItem()));
        dto.setPricePerItem(reserveEntry.getPricePerItem());
        dto.setQuantity(reserveEntry.getQuantity());

        return dto;
    }
}
