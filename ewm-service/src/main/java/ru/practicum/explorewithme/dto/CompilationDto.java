package ru.practicum.explorewithme.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class CompilationDto {
    private List<EventDto> events;
    private long id;
    private boolean pinned;
    private String title;
}
