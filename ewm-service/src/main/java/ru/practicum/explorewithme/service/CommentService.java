package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.dto.category.UpdateCategoryDto;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.CommentShortDto;
import ru.practicum.explorewithme.entity.Comment;

import java.util.List;

public interface CommentService {

    List<CommentDto> getComments(long userId, int from, int size);

    CommentDto getCommentByEventId(long eventId, long commentId);

    Comment getCommentById(long eventId, long commentId);
}
