package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.CategoryDto;
import ru.practicum.explorewithme.dto.NewCategoryDto;
import ru.practicum.explorewithme.dto.UpdateCategoryDto;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.exception.NotEmptyCategoryException;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.repository.CategoryRepository;
import ru.practicum.explorewithme.exception.DuplicatedDataException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.repository.EventRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto body) {
        Category category = CategoryMapper.toCategory(body);
        Category result = save(category);
        return CategoryMapper.toCategoryDto(result);
    }

    private Category save(Category category) {
        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatedDataException("category", "name", category.getName());
        }
    }

    @Override
    @Transactional
    public void deleteCategory(long catId) {
        checkCategoryExistsBy(catId);
        checkCategoryEventsExistsBy(catId);
        categoryRepository.deleteById(catId);
    }

    private void checkCategoryExistsBy(long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("category", id);
        }
    }

    private void checkCategoryEventsExistsBy(long id) {
        if (eventRepository.existsByCategoryId(id)) {
            throw new NotEmptyCategoryException(id);
        }

    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long catId, UpdateCategoryDto body) {
        Category category = findCategoryBy(catId);
        Category update = CategoryMapper.toCategory(category, body);
        Category result = save(update);
        return CategoryMapper.toCategoryDto(result);
    }

    private Category findCategoryBy(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("category", id));
    }
}
