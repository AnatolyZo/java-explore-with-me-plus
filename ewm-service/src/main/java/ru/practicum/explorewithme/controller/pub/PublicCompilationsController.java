package ru.practicum.explorewithme.controller.pub;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.controller.admin.AdminCompilationsController;
import ru.practicum.explorewithme.dto.CompilationDto;
import ru.practicum.explorewithme.service.CompilationsService;

import java.util.List;

@RestController
@RequestMapping(path = PublicCompilationsController.URL_BASE)
@RequiredArgsConstructor
@Validated
@SuppressWarnings("unused")
public class PublicCompilationsController {
    public static final String URL_BASE = "/compilations";
    public static final String PARAM_PINNED = "pinned";
    public static final String PARAM_FROM = "from";
    public static final String PARAM_SIZE = "size";

    private final CompilationsService compilationsService;

    @GetMapping
    public ResponseEntity<List<CompilationDto>> getCompilations(
            @RequestParam(name = PARAM_PINNED, required = false) Boolean pinned,
            @RequestParam(name = PARAM_FROM, required = false, defaultValue = "0")
            @PositiveOrZero int from,
            @RequestParam(name = PARAM_SIZE, required = false, defaultValue = "10")
            @Positive int size
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(compilationsService.getCompilations(pinned, from, size));
    }

    @GetMapping("/{" + AdminCompilationsController.ID_COMPILATION + "}")
    public ResponseEntity<CompilationDto> getCompilation(
            @PathVariable(name = AdminCompilationsController.ID_COMPILATION)
            long compId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(compilationsService.getCompilation(compId));
    }
}
