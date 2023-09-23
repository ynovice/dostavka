package com.github.ynovice.felicita.model.dto.entity;

import com.github.ynovice.felicita.model.entity.Address;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class AddressDto {

    private static final String DATE_TIME_PRESENTATION_FORMAT = "dd.MM.YYYY в HH:mm";

    private Long id;
    private Double lat;
    private Double lon;
    private String representation;
    private String createdAt;

    public static AddressDto fromEntity(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setLat(address.getLat());
        dto.setLon(address.getLon());
        dto.setRepresentation(address.getRepresentation());
        dto.setCreatedAt(
                address.getCreatedAt().format(DateTimeFormatter.ofPattern(DATE_TIME_PRESENTATION_FORMAT))
        );
        return dto;
    }
}
