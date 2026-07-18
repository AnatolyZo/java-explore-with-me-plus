package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.CommentStatus;
import ru.practicum.explorewithme.dto.comment.ModerationAction;

import java.util.List;

public interface CommentService {
    List<CommentDto> searchComments(long adminId,
                                    String text,
                                    List<Long> authorsIds,
                                    String rangeStart,
                                    String rangeEnd,
                                    List<Long> eventId,
                                    List<CommentStatus> states,
                                    int from,
                                    int size);

    CommentDto moderateComment(long adminId, long commentId, ModerationAction action);

    void deleteComment(long userId, long commentId);
}
