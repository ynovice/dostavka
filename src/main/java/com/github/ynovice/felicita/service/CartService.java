package com.github.ynovice.felicita.service;

import com.github.ynovice.felicita.model.entity.Cart;
import lombok.NonNull;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface CartService {

    Cart getByPrincipal(@NonNull OAuth2User principal);

    Cart appendOneItem(Long itemId, @NonNull OAuth2User oAuth2User);

    Cart removeItems(Long itemId, @NonNull OAuth2User oAuth2User);

    Cart removeItems(Long itemId, @NonNull OAuth2User oAuth2User, Integer amount);
}
