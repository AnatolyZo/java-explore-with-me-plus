package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.CommentShortDto;
import ru.practicum.explorewithme.entity.Comment;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.User;

public class CommentMapper {
    public static CommentShortDto toCommetShortDto(Comment comment) {
        return CommentShortDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .author_id(comment.getAuthor() != null ? comment.getAuthor().getId() : 0L)
                .event_id(comment.getEvent() != null ? comment.getAuthor().getId() : 0L)
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .author_id(comment.getAuthor() != null ? comment.getAuthor().getId() : 0L)
                .event_id(comment.getEvent() != null ? comment.getAuthor().getId() : 0L)
                .status(comment.getStatus())
                .moderated(comment.getModerated())
                .moderator_id(comment.getModerator() != null ? comment.getModerator().getId() : 0L)
                .moderation_reason(comment.getModeration_reason())
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

    public static Comment toComment(CommentDto dto) {
        User author = new User(dto.getAuthor_id());
        Event event = new Event(dto.getEvent_id());
        User moderator = new User(dto.getModerator_id());

        return Comment.builder()
                .text(dto.getText())
                .created(dto.getCreated())
                .updated(dto.getUpdated())
                .author(author)
                .event(event)
                .moderated(dto.getModerated())
                .moderator(moderator)
                .moderation_reason(dto.getModeration_reason())
                .build();
    }
}