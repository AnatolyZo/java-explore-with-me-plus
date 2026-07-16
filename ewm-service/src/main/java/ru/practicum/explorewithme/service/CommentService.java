package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.dto.category.UpdateCategoryDto;
import ru.practicum.explorewithme.dto.comment.CommentShortDto;

import java.util.List;

public interface CommentService {

    List<CommentShortDto> getComments(long userId, int from, int size);

    CommentShortDto getComment(long commentId);
}
