package com.github.ynovice.felicita.service;

import com.github.ynovice.felicita.model.entity.Order;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {

    Order orderAllItemsInCart(@NonNull OAuth2User oAuth2User);

    void cancelById(Long id, @NonNull OAuth2User oAuth2User);

    Order getById(Long id, @NonNull OAuth2User oAuth2User);

    Page<Order> getAllByUser(int page, @NonNull OAuth2User oAuth2User);

    Page<Order> getAll(int page);

    void deleteById(Long id);
}
