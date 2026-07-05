package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.CategoryDto;
import ru.practicum.explorewithme.dto.NewCategoryDto;
import ru.practicum.explorewithme.dto.UpdateCategoryDto;
import ru.practicum.explorewithme.entity.Category;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto body);

    void deleteCategory(long catId);

    CategoryDto updateCategory(long catId, UpdateCategoryDto body);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(long catId);

    Category findCategoryBy(long id);
}
