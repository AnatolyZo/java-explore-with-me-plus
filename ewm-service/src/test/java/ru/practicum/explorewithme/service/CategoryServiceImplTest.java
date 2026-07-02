package ru.practicum.explorewithme.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.explorewithme.dto.CategoryDto;
import ru.practicum.explorewithme.dto.NewCategoryDto;
import ru.practicum.explorewithme.dto.UpdateCategoryDto;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.exception.DuplicatedDataException;
import ru.practicum.explorewithme.exception.NotEmptyCategoryException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.repository.CategoryRepository;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.test.ServiceTest;

public class CategoryServiceImplTest extends ServiceTest {
    @InjectMocks
    private CategoryServiceImpl categoryService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private EventRepository eventRepository;

    @Test
    public void createCategory_ReturnsObject() {
        // Arrange
        NewCategoryDto body = buildNewCategoryDto();
        long savedId = 1L;
        Category saved = buildCategory(savedId, body);
        whenSaveReturns(categoryRepository, saved);

        // Act
        CategoryDto actual = categoryService.createCategory(body);

        // Assert
        CategoryDto expected = buildCategoryDto(saved);
        assertEquals(actual, expected);
    }

    @Test
    public void createCategory_ExistingName_DuplicatedDataException() {
        // Arrange
        NewCategoryDto body = buildNewCategoryDto();
        whenSaveThrows(categoryRepository, new DataIntegrityViolationException(""));

        // Act
        Throwable thrown = Assertions.catchThrowable(() -> categoryService.createCategory(body));

        // Assert
        assertException(thrown, DuplicatedDataException.class);
    }

    @Test
    public void deleteCategory() {
        // Arrange
        long id = 1L;
        whenEntityExistIn(categoryRepository);
        whenEventsNotExistIn(id);
        doNothingOnDeleteIn(categoryRepository);

        // Act
        categoryService.deleteCategory(id);

        // Assert
        assertMethodCall(categoryRepository, repository -> repository.deleteById(Mockito.eq(id)));
    }

    @Test
    public void deleteCategory_AbsentCategory_NotFoundException() {
        // Arrange
        long absentId = 1L;
        whenEntityAbsentIn(categoryRepository);

        // Act
        Throwable thrown = Assertions.catchThrowable(() -> categoryService.deleteCategory(absentId));

        // Assert
        assertException(thrown, NotFoundException.class);
    }

    @Test
    public void deleteCategory_CategoryWithEvents_NotEmptyCategoryException() {
        // Arrange
        long id = 1L;
        whenEntityExistIn(categoryRepository);
        whenEventsExistIn(id);

        // Act
        Throwable thrown = Assertions.catchThrowable(() -> categoryService.deleteCategory(id));

        // Assert
        assertException(thrown, NotEmptyCategoryException.class);
    }

    @Test
    public void updateCategory_ReturnsObject() {
        // Arrange
        long savedId = 1L;
        UpdateCategoryDto body = buildUpdateCategoryDto();
        Category saved = buildCategory(savedId, body);
        whenEntityFoundIn(categoryRepository, saved);
        whenSaveReturns(categoryRepository, saved);

        // Act
        CategoryDto actual = categoryService.updateCategory(savedId, body);

        // Assert
        CategoryDto expected = buildCategoryDto(saved);
        assertEquals(actual, expected);
    }

    @Test
    public void updateCategory_AbsentCategory_NotFoundException() {
        // Arrange
        long absentId = 1L;
        UpdateCategoryDto body = buildUpdateCategoryDto();
        whenEntityNotFoundIn(categoryRepository);

        // Act
        Throwable thrown = Assertions.catchThrowable(() -> categoryService.updateCategory(absentId, body));

        // Assert
        assertException(thrown, NotFoundException.class);
    }

    @Test
    public void updateCategory_ExistingName_DuplicatedDataException() {
        // Arrange
        long savedId = 1L;
        UpdateCategoryDto body = buildUpdateCategoryDto();
        Category saved = buildCategory(savedId, body);
        whenEntityFoundIn(categoryRepository, saved);
        whenSaveThrows(categoryRepository, new DataIntegrityViolationException(""));

        // Act
        Throwable thrown = Assertions.catchThrowable(() -> categoryService.updateCategory(savedId, body));

        // Assert
        assertException(thrown, DuplicatedDataException.class);
    }

    private void whenEventsExistIn(long categoryId) {
        Mockito.when(eventRepository.existsByCategoryId(Mockito.eq(categoryId)))
                .thenReturn(true);
    }

    private void whenEventsNotExistIn(long categoryId) {
        Mockito.when(eventRepository.existsByCategoryId(Mockito.eq(categoryId)))
                .thenReturn(false);
    }

    private CategoryDto buildCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    private Category buildCategory(long id, NewCategoryDto newCategoryDto) {
        return Category.builder()
                .id(id)
                .name(newCategoryDto.getName())
                .build();
    }

    private Category buildCategory(long id, UpdateCategoryDto updateCategoryDto) {
        return Category.builder()
                .id(id)
                .name(updateCategoryDto.getName())
                .build();
    }
}
