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
        Comment result = getCommentById(eventId, commentId);
        log.debug("Найден комментарий {}", result);
        return CommentMapper.toCommentDto(result);
    }

    @Override
    public List<CommentDto> getComments(long eventId, int from, int size) {
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.common.pagination.OffsetPageRequest;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.CommentStatus;
import ru.practicum.explorewithme.dto.comment.ModerationAction;
import ru.practicum.explorewithme.entity.Comment;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.mapper.CommentMapper;
import ru.practicum.explorewithme.repository.CommentRepository;
import ru.practicum.explorewithme.repository.UserRepository;
import ru.practicum.explorewithme.repository.specification.AdminCommentSearchSpecification;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceBase implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public List<CommentDto> searchComments(long adminId,
                                           String text,
                                           List<Long> authorsIds,
                                           String rangeStart,
                                           String rangeEnd,
                                           List<Long> eventIds,
                                           List<CommentStatus> states,
                                           int from,
                                           int size) {
        Specification<Comment> spec = new AdminCommentSearchSpecification(text, authorsIds, rangeStart, rangeEnd, eventIds, states);
        Pageable pageable = new OffsetPageRequest(from, size);
        Page<Comment> commentPage = commentRepository.findAll(spec, pageable);
        List<Comment> comments = commentPage.getContent();
        if (comments.isEmpty()) {
            return List.of();
        }

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    public CommentDto moderateComment(long adminId, long commentId, ModerationAction action) {
        User admin = findEntityIn(userRepository, adminId, Entities.USER);
        Comment comment = findEntityIn(commentRepository, commentId, Entities.COMMENT);

        if (action.equals(ModerationAction.APPROVE)) {
            comment.setText(comment.getTextOnModeration());
            comment.setModerator(admin);
            comment.setModerated(LocalDateTime.now());
        }

        comment.setTextOnModeration(null);
        comment.setStatus(CommentStatus.APPROVED);
        commentRepository.save(comment);

        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public void deleteComment(long adminId, long commentId) {
        Comment comment = findEntityIn(commentRepository, commentId, Entities.COMMENT);
        commentRepository.delete(comment);
    }
}
