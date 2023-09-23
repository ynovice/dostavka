package com.github.ynovice.felicita.service;

import com.github.ynovice.felicita.model.dto.request.CreateAddressDto;
import com.github.ynovice.felicita.model.entity.Address;
import lombok.NonNull;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface AddressService {

    Address createAddress(CreateAddressDto requestDto, @NonNull OAuth2User principal);

    void deleteById(Long id, @NonNull OAuth2User principal);
}
