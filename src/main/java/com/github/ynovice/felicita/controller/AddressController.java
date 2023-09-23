package com.github.ynovice.felicita.controller;

import com.github.ynovice.felicita.model.dto.entity.AddressDto;
import com.github.ynovice.felicita.model.dto.request.CreateAddressDto;
import com.github.ynovice.felicita.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressDto> createAddress(@RequestBody CreateAddressDto requestDto,
                                                    @AuthenticationPrincipal OAuth2User principal) {
        return ResponseEntity.ok(
                AddressDto.fromEntity(addressService.createAddress(requestDto, principal)));
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id, @AuthenticationPrincipal OAuth2User principal) {
        addressService.deleteById(id, principal);
    }
}
