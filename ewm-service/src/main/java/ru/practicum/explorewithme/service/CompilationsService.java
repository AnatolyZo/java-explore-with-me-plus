package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.CompilationDto;
import ru.practicum.explorewithme.dto.NewCompilationDto;
import ru.practicum.explorewithme.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationsService {
    CompilationDto createCompilation(NewCompilationDto body);

    void deleteCompilation(long compId);

    CompilationDto updateCompilation(long compId, UpdateCompilationRequest body);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilation(long compId);
}
