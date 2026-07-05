package ru.practicum.explorewithme.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class UserShortDto {
    private Long id;
    private String name;
}
