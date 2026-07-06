package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.CompilationDto;
import ru.practicum.explorewithme.dto.NewCompilationDto;
import ru.practicum.explorewithme.entity.Compilation;

public class CompilationMapper {
    public static Compilation toCompilation(NewCompilationDto body) {
        return Compilation.builder()
                .pinned(body.isPinned())
                .title(body.getTitle())
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .events(
                        compilation.getEvents().stream()
                                .map(event -> EventMapper.mapToEventDto(event.getEvent(), 0))
                                .toList()
                )
                .id(compilation.getId())
                .pinned(compilation.isPinned())
                .title(compilation.getTitle())
                .build();
    }
}
