package com.github.ynovice.felicita.controller;

import com.github.ynovice.felicita.model.dto.entity.OrderDto;
import com.github.ynovice.felicita.model.dto.response.OrdersPageDto;
import com.github.ynovice.felicita.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Secured("ROLE_USER")
    @PostMapping
    public ResponseEntity<OrderDto> orderAllItemsInCart(@AuthenticationPrincipal OAuth2User principal) {
        return ResponseEntity.ok(
                OrderDto.fromEntity(orderService.orderAllItemsInCart(principal))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(@PathVariable Long id, @AuthenticationPrincipal OAuth2User principal) {
        return ResponseEntity.ok(
                OrderDto.fromEntity(orderService.getById(id, principal))
        );
    }

    @GetMapping
    public ResponseEntity<OrdersPageDto> getAllByUser(@RequestParam(defaultValue = "0") int page,
                                                      @AuthenticationPrincipal OAuth2User principal) {
        return ResponseEntity.ok(
                OrdersPageDto.fromEntity(orderService.getAllByUser(page, principal))
        );
    }

    @Secured("ROLE_ADMIN")
    @GetMapping(params = "scope=admin")
    public ResponseEntity<OrdersPageDto> getAll(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(
            OrdersPageDto.fromEntity(orderService.getAll(page))
        );
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        orderService.deleteById(id);
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping(value = "/{id}", params = "action=cancel")
    public void cancelById(@PathVariable Long id, @AuthenticationPrincipal OAuth2User principal) {
        orderService.cancelById(id, principal);
    }
}
