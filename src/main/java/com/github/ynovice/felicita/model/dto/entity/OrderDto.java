package com.github.ynovice.felicita.model.dto.entity;

import com.github.ynovice.felicita.model.entity.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
public class OrderDto extends OrderShortInfoDto {

    private static final String DATE_TIME_PRESENTATION_FORMAT = "dd.MM.YYYY в HH:mm";

    private List<OrderEntryDto> entries;

    public static OrderDto fromEntity(Order order) {

        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setEntries(
                order.getEntries()
                        .stream()
                        .map(OrderEntryDto::fromEntity)
                        .toList()
        );
        dto.setTotalPrice(order.getTotalPrice());
        dto.setTotalItems(order.getTotalItems());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setCreatedAtPresentation(
                order.getCreatedAt().format(DateTimeFormatter.ofPattern(DATE_TIME_PRESENTATION_FORMAT))
        );
        dto.setOwner(UserDto.fromEntity(order.getUser()));

        return dto;
    }
}
