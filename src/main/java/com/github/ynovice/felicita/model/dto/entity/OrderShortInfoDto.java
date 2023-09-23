package com.github.ynovice.felicita.model.dto.entity;

import com.github.ynovice.felicita.model.entity.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class OrderShortInfoDto {

    protected static final String DATE_TIME_PRESENTATION_FORMAT = "dd.MM.YYYY в HH:mm";

    private Long id;
    private Integer totalPrice;
    private Integer totalItems;
    private ZonedDateTime createdAt;
    private String createdAtPresentation;
    private UserDto owner;

    public static OrderShortInfoDto fromEntity(Order order) {

        OrderShortInfoDto dto = new OrderShortInfoDto();
        dto.setId(order.getId());
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
