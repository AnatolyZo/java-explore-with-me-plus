package ru.practicum.explorewithme.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.CommentStatus;
import ru.practicum.explorewithme.entity.Comment;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    @DisplayName("getCommentByEventId должен вернуть комментарий по eventId и commentId")
    void getCommentByEventId_shouldReturnComment() {
        Long eventId = 1L;
        Long commentId = 10L;

        Comment comment = makeComment(commentId, "Текст комментария", eventId);

        when(commentRepository.findByIdAndEventId(commentId, eventId))
                .thenReturn(Optional.of(comment));

        CommentDto result = commentService.getCommentByEventId(eventId, commentId);

        assertNotNull(result);
        assertEquals(commentId, result.getId());
        assertEquals("Текст комментария", result.getText());

        verify(commentRepository).findByIdAndEventId(commentId, eventId);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    @DisplayName("getCommentByEventId должен выбросить NotFoundException, если комментарий не найден")
    void getCommentByEventId_whenCommentNotFound_shouldThrowNotFoundException() {
        Long eventId = 1L;
        Long commentId = 999L;

        when(commentRepository.findByIdAndEventId(commentId, eventId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> commentService.getCommentByEventId(eventId, commentId)
        );

        verify(commentRepository).findByIdAndEventId(commentId, eventId);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    @DisplayName("getComments должен вернуть список комментариев события")
    void getComments_shouldReturnComments() {
        Long eventId = 1L;
        int from = 0;
        int size = 10;

        Comment firstComment = makeComment(1L, "Первый комментарий", eventId);
        Comment secondComment = makeComment(2L, "Второй комментарий", eventId);

        when(commentRepository.findByEventIdWithOffset(eventId, from, size))
                .thenReturn(List.of(firstComment, secondComment));

        List<CommentDto> result = commentService.getComments(eventId, from, size);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals("Первый комментарий", result.get(0).getText());

        assertEquals(2L, result.get(1).getId());
        assertEquals("Второй комментарий", result.get(1).getText());

        verify(commentRepository).findByEventIdWithOffset(eventId, from, size);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    @DisplayName("getComments должен вернуть пустой список, если комментариев нет")
    void getComments_whenCommentsNotFound_shouldReturnEmptyList() {
        Long eventId = 1L;
        int from = 0;
        int size = 10;

        when(commentRepository.findByEventIdWithOffset(eventId, from, size))
                .thenReturn(List.of());

        List<CommentDto> result = commentService.getComments(eventId, from, size);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(commentRepository).findByEventIdWithOffset(eventId, from, size);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    @DisplayName("getComments должен передать в репозиторий корректные параметры пагинации")
    void getComments_shouldPassCorrectPaginationParamsToRepository() {
        Long eventId = 5L;
        int from = 20;
        int size = 50;

        when(commentRepository.findByEventIdWithOffset(eventId, from, size))
                .thenReturn(List.of());

        commentService.getComments(eventId, from, size);

        verify(commentRepository).findByEventIdWithOffset(eventId, from, size);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    @DisplayName("getCommentById должен вернуть Comment entity")
    void getCommentById_shouldReturnCommentEntity() {
        Long eventId = 1L;
        Long commentId = 10L;

        Comment comment = makeComment(commentId, "Комментарий", eventId);

        when(commentRepository.findByIdAndEventId(commentId, eventId))
                .thenReturn(Optional.of(comment));

        Comment result = commentService.getCommentById(eventId, commentId);

        assertNotNull(result);
        assertEquals(commentId, result.getId());
        assertEquals("Комментарий", result.getText());
        assertNotNull(result.getEvent());
        assertEquals(eventId, result.getEvent().getId());

        verify(commentRepository).findByIdAndEventId(commentId, eventId);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    @DisplayName("getCommentById должен выбросить NotFoundException, если комментарий не найден")
    void getCommentById_whenCommentNotFound_shouldThrowNotFoundException() {
        Long eventId = 1L;
        Long commentId = 10L;

        when(commentRepository.findByIdAndEventId(commentId, eventId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> commentService.getCommentById(eventId, commentId)
        );

        verify(commentRepository).findByIdAndEventId(commentId, eventId);
        verifyNoMoreInteractions(commentRepository);
    }

    private Comment makeComment(Long id, String text, Long eventId) {
        Event event = makeEvent(eventId);
        User author = makeUser(100L, "author@mail.com", "Author");
        User moderator = makeUser(200L, "moderator@mail.com", "Moderator");

        return Comment.builder()
                .id(id)
                .text(text)
                .created(LocalDateTime.of(2024, 1, 1, 12, 0))
                .updated(LocalDateTime.of(2024, 1, 1, 13, 0))
                .event(event)
                .author(author)
                .status(CommentStatus.PENDING)
                .moderator(moderator)
                .moderated(LocalDateTime.of(2024, 1, 1, 14, 0))
                .moderation_reason("Комментарий опубликован")
                .build();
    }

    private Event makeEvent(Long id) {
        Event event = new Event();
        event.setId(id);
        return event;
    }

    private User makeUser(Long id, String email, String name) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        return user;
    }
}