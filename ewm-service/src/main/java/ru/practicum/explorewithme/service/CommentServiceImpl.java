package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.comment.*;
import ru.practicum.explorewithme.entity.Comment;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.mapper.CommentMapper;
import ru.practicum.explorewithme.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.explorewithme.common.pagination.OffsetPageRequest;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.UserRepository;
import ru.practicum.explorewithme.repository.specification.AdminCommentSearchSpecification;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceBase implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public CommentShortDto getCommentByEventId(long eventId, long commentId) {
        log.trace("Инициировано получение комментария с id={}", commentId);
        Comment result = getCommentById(eventId, commentId);
        log.debug("Найден комментарий {}", result);
        return CommentMapper.toCommentShortDto(result);
    }

    @Override
    public List<CommentShortDto> getComments(long eventId, int from, int size) {
        log.trace("Иницировано получение комментариев с параметрами from={} и size={}", from, size);
        List<Comment> result = commentRepository.findByEventIdWithOffset(eventId, from, size);
        log.debug("Найдено {} категорий", result.size());
        return result.stream()
                .map(CommentMapper::toCommentShortDto)
                .toList();
    }

    private Comment getCommentById(long eventId, long commentId) {
        return commentRepository.findByIdAndEventId(commentId, eventId)
                .orElseThrow(() -> new NotFoundException(Entities.COMMENT, commentId));
    }

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
    @Transactional
    public CommentDto moderateComment(long adminId, long commentId, ModerationAction action) {
        LocalDateTime now = LocalDateTime.now();
        User admin = findEntityIn(userRepository, adminId, Entities.USER);
        Comment comment = findEntityIn(commentRepository, commentId, Entities.COMMENT);

        if (action.equals(ModerationAction.APPROVE)) {
            comment.setText(comment.getTextOnModeration());
            comment.setUpdated(now);
            comment.setStatus(CommentStatus.APPROVED);
        }

        if (action.equals(ModerationAction.REJECT) && comment.getText() == null) {
            comment.setStatus(CommentStatus.REJECTED);
        } else if (action.equals(ModerationAction.REJECT) && comment.getText() != null) {
            comment.setStatus(CommentStatus.APPROVED);
        }

        comment.setModerator(admin);
        comment.setModerated(now);
        comment.setTextOnModeration(null);
        commentRepository.save(comment);

        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(long adminId, long commentId) {
        Comment comment = findEntityIn(commentRepository, commentId, Entities.COMMENT);
        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByUser(long userId, long commentId) {
        Comment comment = findEntityIn(commentRepository, commentId, Entities.COMMENT);
        checkCommentAuthorship(comment, userId);
        commentRepository.delete(comment);
    }

    @Override
    public List<CommentShortDto> getUsersComments(long userId) {
        checkUserExistence(userId);

        List<Comment> commentsList = commentRepository.findByAuthorId(userId);
        return commentsList.stream()
                .map(CommentMapper::toCommentShortDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentShortDto createComment(long userId, long eventId, NewCommentDto body) {
        User author = findEntityIn(userRepository, userId, Entities.USER);
        Event event = findEntityIn(eventRepository, eventId, Entities.EVENT);
        Comment comment = CommentMapper.toComment(body);
        setFieldsOnCreation(comment, event, author);
        Comment createdComment = commentRepository.save(comment);
        return CommentMapper.toCommentShortDto(createdComment);
    }

    @Override
    @Transactional
    public CommentShortDto updateComment(long userId, long commentId, UpdateCommentDto body) {
        checkUserExistence(userId);
        Comment comment = findEntityIn(commentRepository, commentId, Entities.COMMENT);
        checkCommentAuthorship(comment, userId);
        comment.setTextOnModeration(body.getText());
        comment.setStatus(CommentStatus.PENDING);
        Comment updatedComment = commentRepository.save(comment);
        return CommentMapper.toCommentShortDto(updatedComment);
    }

    private void setFieldsOnCreation(Comment comment, Event event, User author) {
        comment.setCreated(LocalDateTime.now());
        comment.setEvent(event);
        comment.setAuthor(author);
        comment.setStatus(CommentStatus.PENDING);
    }

    private void checkUserExistence(long userId) {
        boolean isUserExists = userRepository.existsById(userId);
        if (!isUserExists) {
            throw new NotFoundException(Entities.USER, userId);
        }
    }

    private void checkCommentAuthorship(Comment comment, long userId) {
        if (comment.getAuthor().getId() != userId) {
            throw new UnavailableUpdateException(Entities.COMMENT.name(), comment.getId());
        }
    }
}
