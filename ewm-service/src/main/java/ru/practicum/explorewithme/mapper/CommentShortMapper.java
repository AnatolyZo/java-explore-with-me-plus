package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.comment.CommentShortDto;
import ru.practicum.explorewithme.entity.Comment;

public class CommentShortMapper {
    public static CommentShortDto toDto(Comment comment) {
        return new CommentShortDto();
    }

    public static Comment toComment(CommentShortDto dto) {
        return new Comment();
    }
}