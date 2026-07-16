package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.comment.CommentShortDto;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.entity.Comment;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.mapper.CommentShortMapper;
import ru.practicum.explorewithme.repository.CommentRepository;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceBase implements CommentService {
    private final CommentRepository commentRepository;

    public List<CommentShortDto> getComments(int from, int size) {
        return List.of();
    }

    public CommentShortDto getComment(long commentId) {
        log.trace("Инициировано получение комментария с id={}", commentId);
        Comment result = findEntityIn(commentRepository, commentId, Entities.COMMENT);
        log.debug("Найден комментарий {}", result);
        return CommentShortMapper.toDto(result);
    }

    @Override
    public List<CommentShortDto> getComments(long userId, int from, int size) {
        log.trace("Иницировано получение комментариев с параметрами from={} и size={}", from, size);
        List<Comment> result = commentRepository.findByEventIdWithOffset(userId, from, size);
        log.debug("Найдено {} категорий", result.size());
        return result.stream()
                .map(CommentShortMapper::toDto)
                .toList();
    }
}
