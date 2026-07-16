package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.dto.category.UpdateCategoryDto;
import ru.practicum.explorewithme.dto.comment.CommentShortDto;
import ru.practicum.explorewithme.entity.Comment;

import java.util.List;

public interface CommentService {

    List<CommentShortDto> getComments(long userId, int from, int size);

    CommentShortDto getCommentByEventId(long eventId, long commentId);

    Comment getCommentById(long eventId, long commentId);
}
