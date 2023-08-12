package com.github.ynovice.felicita.model.dto.entity;

import com.github.ynovice.felicita.model.entity.Reserve;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
public class ReserveDto extends ReserveShortInfoDto{

    private static final String DATE_TIME_PRESENTATION_FORMAT = "dd.MM.YYYY в HH:mm";

    private List<ReserveEntryDto> entries;

    public static ReserveDto fromEntity(Reserve reserve) {

        ReserveDto dto = new ReserveDto();
        dto.setId(reserve.getId());
        dto.setEntries(
                reserve.getEntries()
                        .stream()
                        .map(ReserveEntryDto::fromEntity)
                        .toList()
        );
        dto.setTotalPrice(reserve.getTotalPrice());
        dto.setTotalItems(reserve.getTotalItems());
        dto.setCreatedAt(reserve.getCreatedAt());
        dto.setCreatedAtPresentation(
                reserve.getCreatedAt().format(DateTimeFormatter.ofPattern(DATE_TIME_PRESENTATION_FORMAT))
        );
        dto.setOwner(UserDto.fromEntity(reserve.getUser()));

        return dto;
    }
}
