package com.github.ynovice.felicita.service.impl;

import com.github.ynovice.felicita.exception.BadRequestException;
import com.github.ynovice.felicita.exception.NotAuthorizedException;
import com.github.ynovice.felicita.exception.NotFoundException;
import com.github.ynovice.felicita.model.dto.request.OrderAllItemsInCartRequestDto;
import com.github.ynovice.felicita.model.entity.*;
import com.github.ynovice.felicita.repository.*;
import com.github.ynovice.felicita.service.CartService;
import com.github.ynovice.felicita.service.ItemService;
import com.github.ynovice.felicita.service.OrderService;
import com.github.ynovice.felicita.service.UserService;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final int ORDERS_PER_PAGE = 30;

    private final OrderRepository orderRepository;

    private final CartService cartService;
    private final ItemService itemService;
    private final UserService userService;

    private final CartRepository cartRepository;
    private final CartEntryRepository cartEntryRepository;
    private final OrderEntryRepository orderEntryRepository;
    private final AddressRepository addressRepository;

    @Override
    @Transactional
    public Order orderAllItemsInCart(OrderAllItemsInCartRequestDto requestDto, @NonNull OAuth2User oAuth2User) {

        User user = userService.getUser(oAuth2User);
        Cart cart = cartService.getByPrincipal(oAuth2User);

        if(cart.getTotalItems() == 0) {
            throw new BadRequestException("Ваша корзина пуста");
        }

        Address address = addressRepository.findById(requestDto.getAddressId())
                .orElseThrow(() -> new BadRequestException("Выбранный адрес не существует"));

        if(!address.getUser().equals(user)) {
            throw new NotAuthorizedException("Вы не можете выбрать чужой адрес");
        }

        Order order = createAndLinkOrder(cart, address);

        cart.setTotalItems(0);
        cart.setTotalPrice(0);

        for(CartEntry cartEntry : cart.getEntries())
            createAndLinkOrderEntry(cartEntry, order);

        cart.getEntries().clear();
        cartEntryRepository.deleteAllByCart(cart);

        cartRepository.saveAndFlush(cart);
        orderRepository.saveAndFlush(order);

        itemService.updateItemQuantitiesAfterOrder(order);

        return order;
    }

    @Override
    public void cancelById(Long id, @NonNull OAuth2User oAuth2User) {

        Order order = getById(id, oAuth2User);

        for(OrderEntry orderEntry : order.getEntries()) {

            Item item = orderEntry.getItem();

            item.updateQuantity(orderEntry.getQuantity());

            itemService.save(item);
        }

        deleteById(order.getId());
    }

    @Override
    public Order getById(Long id, @NonNull OAuth2User oAuth2User) {

        User user = userService.getUser(oAuth2User);

        Order order = orderRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        if(!order.getUser().equals(user) && user.getRole() != Role.ADMIN)
            throw new NotAuthorizedException("Вы не можете просматривать этот резерв");

        updateOrder(order);

        return order;
    }

    @Override
    public Page<Order> getAllByUser(int page, @NonNull OAuth2User oAuth2User) {

        Page<Order> ordersPage = orderRepository.findAllByUser(
                userService.getUser(oAuth2User),
                PageRequest.of(page, ORDERS_PER_PAGE)
        );
        ordersPage.getContent().forEach(this::updateOrder);

        return ordersPage;
    }

    @Override
    public Page<Order> getAll(int page) {

        Page<Order> ordersPage = orderRepository.findAll(PageRequest.of(page, ORDERS_PER_PAGE));
        ordersPage.getContent().forEach(this::updateOrder);

        return ordersPage;
    }

    @Override
    public void deleteById(Long id) {

        Order order = orderRepository.findById(id).orElseThrow(NotFoundException::new);

        List<OrderEntry> orderEntriesCopy = new ArrayList<>(order.getEntries());

        order.getEntries().clear();
        orderEntryRepository.deleteAll(orderEntriesCopy);

        orderRepository.deleteById(id);
    }

    private void updateOrder(@NonNull Order order) {

        order.setTotalPrice(0);
        order.setTotalItems(0);

        for(OrderEntry orderEntry : order.getEntries()) {
            order.updateTotalPrice(orderEntry.getPricePerItem() * orderEntry.getQuantity());
            order.updateTotalItems(orderEntry.getQuantity());
        }
    }

    private Order createAndLinkOrder(@NonNull Cart cart, Address address) {

        User user = cart.getUser();

        Order order = new Order();
        order.setEntries(new ArrayList<>());
        order.setTotalPrice(cart.getTotalPrice());
        order.setTotalItems(cart.getTotalItems());
        order.setCreatedAt(ZonedDateTime.now());
        order.setAddress(address);
        order.setUser(user);

        user.getOrders().add(order);

        return order;
    }

    private void createAndLinkOrderEntry(@NonNull CartEntry cartEntry, @NonNull Order order) {

        OrderEntry orderEntry = new OrderEntry();
        orderEntry.setItem(cartEntry.getItem());
        orderEntry.setPricePerItem(cartEntry.getItem().getPrice());

        orderEntry.setQuantity(cartEntry.getQuantity());

        orderEntry.setOrder(order);
        order.getEntries().add(orderEntry);
    }
}
