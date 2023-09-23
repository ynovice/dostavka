package com.github.ynovice.felicita.service.impl;

import com.github.ynovice.felicita.exception.NotAuthorizedException;
import com.github.ynovice.felicita.exception.NotFoundException;
import com.github.ynovice.felicita.model.dto.request.CreateAddressDto;
import com.github.ynovice.felicita.model.entity.Address;
import com.github.ynovice.felicita.model.entity.User;
import com.github.ynovice.felicita.repository.AddressRepository;
import com.github.ynovice.felicita.service.AddressService;
import com.github.ynovice.felicita.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;

    @Override
    public Address createAddress(CreateAddressDto requestDto, @NonNull OAuth2User principal) {

        User user = userService.getUser(principal);

        Address address = new Address();
        address.setLat(requestDto.getLat());
        address.setLon(requestDto.getLon());
        address.setRepresentation(requestDto.getRepresentation());
        address.setCreatedAt(ZonedDateTime.now());
        address.setUser(user);

        addressRepository.save(address);

        return address;
    }

    @Override
    public void deleteById(Long id, @NonNull OAuth2User principal) {

        Address address = addressRepository
                .findById(id)
                .orElseThrow(NotFoundException::new);

        User user = userService.getUser(principal);

        if(!address.getUser().equals(user)) {
            throw new NotAuthorizedException("Вы не можете удалить этот адрес");
        }

        user.getAddresses().remove(address);
        userService.save(user);
        addressRepository.delete(address);
    }
}
