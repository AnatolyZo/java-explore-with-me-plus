package ru.practicum.explorewithme.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.ExploreWithMeMainService;
import ru.practicum.explorewithme.dto.*;
import ru.practicum.explorewithme.service.CompilationsService;

@RestController
@RequestMapping(path = AdminCompilationsController.URL_BASE)
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AdminCompilationsController {
    public static final String URL_BASE = ExploreWithMeMainService.URL_ADMIN + "/compilations";
    public static final String ID_COMPILATION = "compId";

    private final CompilationsService compilationsService;

    @PostMapping
    public ResponseEntity<CompilationDto> createCompilation(@RequestBody @Valid NewCompilationDto body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(compilationsService.createCompilation(body));
    }

    @DeleteMapping("/{" + ID_COMPILATION + "}")
    public ResponseEntity<Void> deleteCompilation(@PathVariable(name = ID_COMPILATION) long compId) {
        compilationsService.deleteCompilation(compId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(null);
    }

    @PatchMapping("/{" + ID_COMPILATION + "}")
    public ResponseEntity<CompilationDto> updateCompilation(@PathVariable(name = ID_COMPILATION) long compId,
                                                      @RequestBody @Valid UpdateCompilationRequest body) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(compilationsService.updateCompilation(compId, body));
    }
}
