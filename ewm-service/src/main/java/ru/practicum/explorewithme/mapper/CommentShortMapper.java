package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.comment.CommentShortDto;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.entity.Comment;

public class CommentShortMapper {
    public static CommentShortDto toDto(Comment comment) {

        return CommentShortDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .build();
    }

    public static Comment toComment(CommentShortDto dto) {
        return Comment.builder()
                .text(dto.getText())
                .build();
    }
}