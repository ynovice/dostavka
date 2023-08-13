package com.github.ynovice.felicita.service.impl;

import com.github.ynovice.felicita.exception.BadRequestException;
import com.github.ynovice.felicita.model.entity.Cart;
import com.github.ynovice.felicita.model.entity.CartEntry;
import com.github.ynovice.felicita.model.entity.Item;
import com.github.ynovice.felicita.model.entity.User;
import com.github.ynovice.felicita.repository.CartEntryRepository;
import com.github.ynovice.felicita.repository.CartRepository;
import com.github.ynovice.felicita.repository.ItemRepository;
import com.github.ynovice.felicita.service.CartService;
import com.github.ynovice.felicita.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final UserService userService;

    private final ItemRepository itemRepository;
    private final CartEntryRepository cartEntryRepository;

    @Override
    public Cart getByPrincipal(@NonNull OAuth2User principal) {

        User user = userService.getUser(principal);

        createAndLinkCartIfNotExists(user);
        Cart cart = user.getCart();

        updateCart(cart);

        cartRepository.saveAndFlush(cart);

        return cart;
    }

    @Override
    public Cart appendOneItem(Long itemId, @NonNull OAuth2User oAuth2User) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BadRequestException("Выбранный товар не существует"));

        Cart cart = getByPrincipal(oAuth2User);
        CartEntry cartEntry = cart.getCartEntryByItem(item);

        if(cartEntry.getQuantity() + 1 > item.getQuantity()) {
            throw new BadRequestException("На складе есть только " + item.getQuantity() + " единиц товара");
        }

        cartEntry.updateQuantity(1);
        cart.updateTotalItems(1);
        cart.updateTotalPrice(item.getPrice());

        cartRepository.saveAndFlush(cart);
        return cart;
    }

    @Override
    public Cart removeItems(Long itemId, @NonNull OAuth2User oAuth2User) {
        return removeItems(itemId, oAuth2User, null);
    }

    @Override
    public Cart removeItems(Long itemId, @NonNull OAuth2User oAuth2User, Integer amount) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BadRequestException("Выбранный товар не существует"));

        Cart cart = getByPrincipal(oAuth2User);
        CartEntry cartEntry = cart.getCartEntryByItem(item);

        if(amount == null)
            amount = cartEntry.getQuantity();

        if(amount <= 0)
            throw new BadRequestException("Количество удаляемых товаров должно быть больше 0");

        if(cartEntry.getQuantity() < amount)
            throw new BadRequestException("У вас в корзине нет " + amount + " единиц такого товара");

        cartEntry.updateQuantity(-amount);

        if(cartEntry.getQuantity() == 0) {
            cart.getEntries().remove(cartEntry);
            cartEntryRepository.delete(cartEntry);
        }

        cart.updateTotalItems(-amount);
        cart.updateTotalPrice(-amount * item.getPrice());

        cartRepository.saveAndFlush(cart);

        return cart;
    }

    private void updateCart(@NonNull Cart cart) {

        List<CartEntry> cartEntriesToDelete = new ArrayList<>();

        for(CartEntry cartEntry : cart.getEntries()) {

            Item item = cartEntry.getItem();
            
            int amountOfItemsGone = cartEntry.getQuantity() - item.getQuantity();
            
            if(amountOfItemsGone > 0) {
                cartEntry.setPrevQuantity(cartEntry.getQuantity());
                cartEntry.updateQuantity(-amountOfItemsGone);
            }

            if(cartEntry.getQuantity() == 0)
                cartEntriesToDelete.add(cartEntry);
        }

        for(CartEntry cartEntry : cartEntriesToDelete) {
            cart.getEntries().remove(cartEntry);
            cartEntryRepository.delete(cartEntry);
        }

        cart.setTotalPrice(0);
        cart.setTotalItems(0);

        for(CartEntry cartEntry : cart.getEntries()) {

            cart.updateTotalItems(cartEntry.getQuantity());
            cart.updateTotalPrice(cartEntry.getQuantity() * cartEntry.getItem().getPrice());
        }
    }

    private void createAndLinkCartIfNotExists(@NonNull User user) {

        if(user.getCart() == null) {

            Cart cart = new Cart();
            cart.setEntries(new ArrayList<>());
            cart.setTotalPrice(0);
            cart.setTotalItems(0);
            cart.setUser(user);

            user.setCart(cart);
        }
    }
}
