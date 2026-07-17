package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.comment.CommentShortDto;
import ru.practicum.explorewithme.entity.Comment;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.User;

public class CommentMapper {
    public static CommentShortDto toCommitShortDto(Comment comment) {

        return CommentShortDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .author_id(comment.getAuthor() != null ? comment.getAuthor().getId() : null)
                .event_id(comment.getEvent() != null ? comment.getAuthor().getId() : null)
                .build();
    }

    public static Comment toComment(CommentShortDto dto) {
        User author = new User(dto.getAuthor_id());
        Event event = new Event(dto.getEvent_id());

        return Comment.builder()
                .text(dto.getText())
                .created(dto.getCreated())
                .updated(dto.getUpdated())
                .author(author)
                .event(event)
                .build();
    }
}