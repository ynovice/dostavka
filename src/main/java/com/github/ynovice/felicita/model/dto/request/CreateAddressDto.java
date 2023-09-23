package com.github.ynovice.felicita.model.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAddressDto {

    private Double lat;
    private Double lon;
    private String representation;
}
