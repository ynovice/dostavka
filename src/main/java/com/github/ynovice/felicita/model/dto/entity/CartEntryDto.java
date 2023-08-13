package com.github.ynovice.felicita.model.dto.entity;

import com.github.ynovice.felicita.model.entity.CartEntry;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class CartEntryDto {

    private Long id;
    private Long itemId;
    private Integer quantity;
    private ItemDto item;

    public static CartEntryDto fromEntity(@NonNull CartEntry cartEntry) {

        CartEntryDto dto = new CartEntryDto();

        dto.setId(cartEntry.getId());
        dto.setItemId(cartEntry.getItemId());
        dto.setQuantity(cartEntry.getQuantity());
        dto.setItem(ItemDto.fromEntity(cartEntry.getItem()));

        return dto;
    }
}
