package com.github.ynovice.felicita.service.impl;

import com.github.ynovice.felicita.exception.BadRequestException;
import com.github.ynovice.felicita.exception.NotAuthorizedException;
import com.github.ynovice.felicita.exception.NotFoundException;
import com.github.ynovice.felicita.model.entity.*;
import com.github.ynovice.felicita.repository.CartEntryRepository;
import com.github.ynovice.felicita.repository.CartRepository;
import com.github.ynovice.felicita.repository.ReserveEntryRepository;
import com.github.ynovice.felicita.repository.ReserveRepository;
import com.github.ynovice.felicita.service.CartService;
import com.github.ynovice.felicita.service.ItemService;
import com.github.ynovice.felicita.service.ReserveService;
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
public class ReserveServiceImpl implements ReserveService {

    private static final int RESERVES_PER_PAGE = 30;

    private final ReserveRepository reserveRepository;

    private final CartService cartService;
    private final ItemService itemService;
    private final UserService userService;

    private final CartRepository cartRepository;
    private final CartEntryRepository cartEntryRepository;
    private final ReserveEntryRepository reserveEntryRepository;

    @Override
    @Transactional
    public Reserve reserveAllItemsInCart(@NonNull OAuth2User oAuth2User) {

        Cart cart = cartService.getByPrincipal(oAuth2User);

        if(cart.getTotalItems() == 0) {
            throw new BadRequestException("Ваша корзина пуста");
        }

        Reserve reserve = createAndLinkReserve(cart);

        cart.setTotalItems(0);
        cart.setTotalPrice(0);

        for(CartEntry cartEntry : cart.getEntries())
            createAndLinkReserveEntry(cartEntry, reserve);

        cart.getEntries().clear();
        cartEntryRepository.deleteAllByCart(cart);

        cartRepository.saveAndFlush(cart);
        reserveRepository.saveAndFlush(reserve);

        itemService.updateItemQuantitiesAfterReserve(reserve);

        return reserve;
    }

    @Override
    public void cancelById(Long id, @NonNull OAuth2User oAuth2User) {

        Reserve reserve = getById(id, oAuth2User);

        for(ReserveEntry reserveEntry : reserve.getEntries()) {

            Item item = reserveEntry.getItem();

            item.updateQuantity(reserveEntry.getQuantity());

            itemService.save(item);
        }

        deleteById(reserve.getId());
    }

    @Override
    public Reserve getById(Long id, @NonNull OAuth2User oAuth2User) {

        User user = userService.getUser(oAuth2User);

        Reserve reserve = reserveRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        if(!reserve.getUser().equals(user) && user.getRole() != Role.ADMIN)
            throw new NotAuthorizedException("Вы не можете просматривать этот резерв");

        updateReserve(reserve);

        return reserve;
    }

    @Override
    public Page<Reserve> getAllByUser(int page, @NonNull OAuth2User oAuth2User) {

        Page<Reserve> reservesPage = reserveRepository.findAllByUser(
                userService.getUser(oAuth2User),
                PageRequest.of(page, RESERVES_PER_PAGE)
        );
        reservesPage.getContent().forEach(this::updateReserve);

        return reservesPage;
    }

    @Override
    public Page<Reserve> getAll(int page) {

        Page<Reserve> reservesPage = reserveRepository.findAll(PageRequest.of(page, RESERVES_PER_PAGE));
        reservesPage.getContent().forEach(this::updateReserve);

        return reservesPage;
    }

    @Override
    public void deleteById(Long id) {

        Reserve reserve = reserveRepository.findById(id).orElseThrow(NotFoundException::new);

        List<ReserveEntry> reserveEntriesCopy = new ArrayList<>(reserve.getEntries());

        reserve.getEntries().clear();
        reserveEntryRepository.deleteAll(reserveEntriesCopy);

        reserveRepository.deleteById(id);
    }

    private void updateReserve(@NonNull Reserve reserve) {

        reserve.setTotalPrice(0);
        reserve.setTotalItems(0);

        for(ReserveEntry reserveEntry : reserve.getEntries()) {
            reserve.updateTotalPrice(reserveEntry.getPricePerItem() * reserveEntry.getQuantity());
            reserve.updateTotalItems(reserveEntry.getQuantity());
        }
    }

    private Reserve createAndLinkReserve(@NonNull Cart cart) {

        User user = cart.getUser();

        Reserve reserve = new Reserve();
        reserve.setEntries(new ArrayList<>());
        reserve.setTotalPrice(cart.getTotalPrice());
        reserve.setTotalItems(cart.getTotalItems());
        reserve.setCreatedAt(ZonedDateTime.now());
        reserve.setUser(user);

        user.getReserves().add(reserve);

        return reserve;
    }

    private void createAndLinkReserveEntry(@NonNull CartEntry cartEntry, @NonNull Reserve reserve) {

        ReserveEntry reserveEntry = new ReserveEntry();
        reserveEntry.setItem(cartEntry.getItem());
        reserveEntry.setPricePerItem(cartEntry.getItem().getPrice());

        reserveEntry.setQuantity(cartEntry.getQuantity());

        reserveEntry.setReserve(reserve);
        reserve.getEntries().add(reserveEntry);
    }
}
