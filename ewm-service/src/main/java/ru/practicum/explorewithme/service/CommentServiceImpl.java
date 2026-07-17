package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public CommentShortDto getCommentByEventId(long eventId, long commentId) {
        log.trace("Инициировано получение комментария с id={}", commentId);
        Comment result = getCommentById(eventId, commentId);
        log.debug("Найден комментарий {}", result);
        return CommentMapper.toCommitShortDto(result);
    }

    @Override
    public List<CommentShortDto> getComments(long eventId, int from, int size) {
        log.trace("Иницировано получение комментариев с параметрами from={} и size={}", from, size);
        List<Comment> result = commentRepository.findByEventIdWithOffset(eventId, from, size);
        log.debug("Найдено {} категорий", result.size());
        return result.stream()
                .map(CommentMapper::toCommitShortDto)
                .toList();
    }

    @Override
    public Comment getCommentById(long eventId, long commentId) {
        return commentRepository.findByIdAndEventId(commentId, eventId)
                .orElseThrow(() -> new NotFoundException(Entities.COMMENT, commentId));
    }
}
