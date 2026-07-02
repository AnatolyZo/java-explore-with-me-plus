package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.CategoryDto;
import ru.practicum.explorewithme.dto.NewCategoryDto;
import ru.practicum.explorewithme.dto.UpdateCategoryDto;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.exception.DuplicatedDataException;
import ru.practicum.explorewithme.exception.NotEmptyCategoryException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.repository.CategoryRepository;
import ru.practicum.explorewithme.repository.EventRepository;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto body) {
        log.trace("Инициировано сохранение категории. Тело запроса: {}", body);
        Category category = CategoryMapper.toCategory(body);
        log.debug("Тело запроса {} преобразовано в категорию {}", body, category);
        Category result = save(category);
        log.debug("Категория {} сохранена", body);
        return CategoryMapper.toCategoryDto(result);
    }

    private Category save(Category category) {
        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            log.info("Не удалось сохранить категорию {}", category);
            throw new DuplicatedDataException("category", "name", category.getName());
        }
    }

    @Override
    @Transactional
    public void deleteCategory(long catId) {
        log.trace("Инициировано удаление категории с id={}", catId);
        checkCategoryExistsBy(catId);
        checkCategoryEventsExistsBy(catId);
        categoryRepository.deleteById(catId);
        log.debug("Категория с id={} удалена", catId);
    }

    private void checkCategoryExistsBy(long id) {
        if (!categoryRepository.existsById(id)) {
            log.info("Категория с id={} не найдена", id);
            throw new NotFoundException("category", id);
        }
    }

    private void checkCategoryEventsExistsBy(long id) {
        if (eventRepository.existsByCategoryId(id)) {
            log.info("Категория с id={} содержит события", id);
            throw new NotEmptyCategoryException(id);
        }

    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long catId, UpdateCategoryDto body) {
        log.trace("Инициировано сохранение категории с id={}. Тело запроса: {}", catId, body);
        Category category = findCategoryBy(catId);
        Category update = CategoryMapper.toCategory(category, body);
        log.debug("Создано обновление категории: {}", update);
        Category result = save(update);
        log.debug("Обновление {} сохранено", update);
        return CategoryMapper.toCategoryDto(result);
    }

    private Category findCategoryBy(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("category", id));
    }
}
