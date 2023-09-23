package com.github.ynovice.felicita.model.dto.response;

import com.github.ynovice.felicita.model.dto.entity.PaginationMeta;
import com.github.ynovice.felicita.model.dto.entity.OrderShortInfoDto;
import com.github.ynovice.felicita.model.entity.Order;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class OrdersPageDto {

    private List<OrderShortInfoDto> orders;
    private PaginationMeta paginationMeta;

    public static OrdersPageDto fromEntity(Page<Order> ordersPage) {

        OrdersPageDto dto = new OrdersPageDto();
        dto.setOrders(ordersPage.stream().map(OrderShortInfoDto::fromEntity).toList());
        dto.setPaginationMeta(PaginationMeta.fromEntity(ordersPage));

        return dto;
    }
}
