package ru.practicum.explorewithme.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.practicum.explorewithme.common.pagination.OffsetPageRequest;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.CommentStatus;
import ru.practicum.explorewithme.dto.comment.ModerationAction;
import ru.practicum.explorewithme.entity.Comment;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.repository.CommentRepository;
import ru.practicum.explorewithme.repository.UserRepository;
import ru.practicum.explorewithme.repository.specification.AdminCommentSearchSpecification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTests {
    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    private final long ADMIN_ID = 1L;
    private final long COMMENT_ID = 2L;
    private final long EVENT_ID = 3L;
    private final long AUTHOR_ID = 4L;

    private User admin;
    private User author;
    private Comment comment;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(ADMIN_ID).build();
        author = User.builder().id(AUTHOR_ID).build();

        comment = Comment.builder()
                .id(COMMENT_ID)
                .text("Старый текст")
                .textOnModeration("Новый текст на модерации")
                .created(LocalDateTime.now())
                .event(Event.builder().id(EVENT_ID).build())
                .author(author)
                .status(CommentStatus.PENDING)
                .build();
    }

    @Test
    void searchComments_returnsEmptyList_whenNoCommentsFound() {
        Page<Comment> emptyPage = new PageImpl<>(List.of());
        when(commentRepository.findAll(any(AdminCommentSearchSpecification.class), any(OffsetPageRequest.class)))
                .thenReturn(emptyPage);

        List<CommentDto> result = commentService.searchComments(ADMIN_ID, null, null, null, null, null, null, 0, 10);

        assertThat(result).isEmpty();
        verify(commentRepository).findAll(any(AdminCommentSearchSpecification.class), any(OffsetPageRequest.class));
    }

    @Test
    void searchComments_returnsMappedDtos_whenCommentsFound() {
        Event event1 = Event.builder()
                .id(1L)
                .build();

        Event event2 = Event.builder()
                .id(2L)
                .build();

        Comment comment1 = Comment.builder()
                .id(1L)
                .text("Текст 1")
                .event(event1)
                .author(author)
                .moderator(admin)
                .build();
        Comment comment2 = Comment.builder()
                .id(2L)
                .text("Текст 2")
                .event(event2)
                .author(author)
                .moderator(admin)
                .build();
        Page<Comment> page = new PageImpl<>(List.of(comment1, comment2));

        when(commentRepository.findAll(any(AdminCommentSearchSpecification.class), any(OffsetPageRequest.class))).thenReturn(page);

        List<CommentDto> result = commentService.searchComments(ADMIN_ID, "поиск", List.of(AUTHOR_ID), "2024-01-01 10:00:00", "2024-12-31 10:00:00", List.of(EVENT_ID), null, 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getText()).isEqualTo("Текст 1");
        assertThat(result.get(1).getText()).isEqualTo("Текст 2");
        verify(commentRepository, times(1)).findAll(any(AdminCommentSearchSpecification.class), any(OffsetPageRequest.class));
    }

    @Test
    void moderateComment_approveAction_acceptsPendingTextAndClearsDraft() {
        Event event = Event.builder()
                .id(1L)
                .build();

        String pendingText = "Текст после правки";
        comment = Comment.builder()
                .id(1L)
                .text("Старый текст")
                .event(event)
                .author(author)
                .moderator(admin)
                .textOnModeration(pendingText)
                .status(CommentStatus.PENDING) // или любой другой, не важно
                .build();

        when(userRepository.findById(eq(ADMIN_ID))).thenReturn(Optional.of(admin));
        when(commentRepository.findById(eq(COMMENT_ID))).thenReturn(Optional.of(comment));

        CommentDto dto = commentService.moderateComment(ADMIN_ID, COMMENT_ID, ModerationAction.APPROVE);

        verify(commentRepository).save(comment);
        assertThat(dto.getText()).isEqualTo(pendingText);
        assertThat(comment.getTextOnModeration()).isNull();
        assertThat(comment.getStatus()).isEqualTo(CommentStatus.APPROVED);
        assertThat(comment.getModerator()).isEqualTo(admin);
        assertThat(comment.getModerated()).isNotNull();
    }

    @Test
    void moderateComment_rejectAction_keepsOriginalTextAndClearsDraft() {
        Event event = Event.builder()
                .id(1L)
                .build();

        String originalText = "Старый текст";
        String pendingText = "Неудачная правка";
        comment = Comment.builder()
                .id(1L)
                .text(originalText)
                .event(event)
                .author(author)
                .moderator(admin)
                .moderated(LocalDateTime.now())
                .textOnModeration(pendingText)
                .status(CommentStatus.PENDING)
                .build();

        when(userRepository.findById(eq(ADMIN_ID))).thenReturn(Optional.of(admin));
        when(commentRepository.findById(eq(COMMENT_ID))).thenReturn(Optional.of(comment));

        CommentDto dto = commentService.moderateComment(ADMIN_ID, COMMENT_ID, ModerationAction.REJECT);

        verify(commentRepository).save(comment);
        assertThat(dto.getText()).isEqualTo(originalText);
        assertThat(comment.getTextOnModeration()).isNull();
        assertThat(comment.getStatus()).isEqualTo(CommentStatus.APPROVED);
        assertThat(comment.getModerator()).isEqualTo(admin);
        assertThat(comment.getModerated()).isNotNull();
    }

    @Test
    void moderateComment_throwsNotFound_whenCommentDoesNotExist() {
        when(userRepository.findById(eq(ADMIN_ID))).thenReturn(Optional.of(admin));
        when(commentRepository.findById(eq(COMMENT_ID))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.moderateComment(ADMIN_ID, COMMENT_ID, ModerationAction.APPROVE))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("COMMENT with id=2 not exists");
    }

    @Test
    void moderateComment_throwsNotFound_whenAdminDoesNotExist() {
        when(userRepository.findById(eq(ADMIN_ID))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.moderateComment(ADMIN_ID, COMMENT_ID, ModerationAction.APPROVE))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("USER with id=1 not exists");
    }

    @Test
    void deleteComment_deletesComment_whenExists() {
        when(commentRepository.findById(eq(COMMENT_ID))).thenReturn(Optional.of(comment));

        commentService.deleteComment(ADMIN_ID, COMMENT_ID);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_throwsNotFound_whenCommentDoesNotExist() {
        when(commentRepository.findById(eq(COMMENT_ID))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(ADMIN_ID, COMMENT_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("COMMENT with id=2 not exists");
    }
}
