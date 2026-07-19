package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.CommentShortDto;
import ru.practicum.explorewithme.entity.Comment;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.CommentMapper;
import ru.practicum.explorewithme.repository.CommentRepository;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceBase implements CommentService {
    private final CommentRepository commentRepository;

    public CommentDto getCommentByEventId(long eventId, long commentId) {
        log.trace("Инициировано получение комментария с id={}", commentId);

        raiseExceptionIfNegative(eventId);
        raiseExceptionIfNegative(commentId);

        Comment result = getCommentById(eventId, commentId);
        log.debug("Найден комментарий {}", result);
        return CommentMapper.toCommentDto(result);
    }

    @Override
    public List<CommentDto> getComments(long eventId, int from, int size) {
        raiseExceptionIfNegative(eventId);

        log.trace("Иницировано получение комментариев с параметрами from={} и size={}", from, size);
        List<Comment> result = commentRepository.findByEventIdWithOffset(eventId, from, size);
        log.debug("Найдено {} категорий", result.size());
        return result.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    public Comment getCommentById(long eventId, long commentId) {
        return commentRepository.findByIdAndEventId(commentId, eventId)
                .orElseThrow(() -> new NotFoundException(Entities.COMMENT, commentId));
    }

    private void raiseExceptionIfNegative(long value) {
        if (value <= 0L) {
            throw new RuntimeException("Идентификатор равен нулю или отрицательное значение.");
        }
    }
}
