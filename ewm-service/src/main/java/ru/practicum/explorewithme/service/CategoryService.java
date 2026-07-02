package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.CategoryDto;
import ru.practicum.explorewithme.dto.NewCategoryDto;
import ru.practicum.explorewithme.dto.UpdateCategoryDto;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto body);

    void deleteCategory(long catId);

    CategoryDto updateCategory(long catId, UpdateCategoryDto body);
}
