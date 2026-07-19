package ru.practicum.explorewithme.controller.pub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.CommentStatus;
import ru.practicum.explorewithme.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.explorewithme.controller.ControllerConstants.*;

@WebMvcTest(PublicCommentsController.class)
class PublicCommentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @Test
    @DisplayName("GET /events/{eventId}/comments должен вернуть список комментариев")
    void getComments_shouldReturnComments() throws Exception {
        long eventId = 1L;
        int from = 0;
        int size = 10;

        CommentDto firstComment = makeCommentDto(
                1L,
                "Первый комментарий",
                eventId,
                100L
        );

        CommentDto secondComment = makeCommentDto(
                2L,
                "Второй комментарий",
                eventId,
                101L
        );

        when(commentService.getComments(eventId, from, size))
                .thenReturn(List.of(firstComment, secondComment));

        mockMvc.perform(get(URL_EVENTS + "/{eventId}" + URL_COMMENTS, eventId)
                        .param(PARAM_FROM, String.valueOf(from))
                        .param(PARAM_SIZE, String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))

                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].text").value("Первый комментарий"))
                .andExpect(jsonPath("$[0].event_id").value(eventId))
                .andExpect(jsonPath("$[0].author_id").value(100L))
                .andExpect(jsonPath("$[0].state").value("PENDING"))
                .andExpect(jsonPath("$[0].moderator_id").value(200L))
                .andExpect(jsonPath("$[0].moderation_reason").value("Комментарий опубликован"))

                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].text").value("Второй комментарий"))
                .andExpect(jsonPath("$[1].event_id").value(eventId))
                .andExpect(jsonPath("$[1].author_id").value(101L))
                .andExpect(jsonPath("$[1].state").value("PENDING"))
                .andExpect(jsonPath("$[1].moderator_id").value(200L))
                .andExpect(jsonPath("$[1].moderation_reason").value("Комментарий опубликован"));

        verify(commentService).getComments(eventId, from, size);
    }

    @Test
    @DisplayName("GET /events/{eventId}/comments должен использовать from=0 и size=10 по умолчанию")
    void getComments_withoutPaginationParams_shouldUseDefaultValues() throws Exception {
        long eventId = 1L;

        CommentDto comment = makeCommentDto(
                1L,
                "Комментарий",
                eventId,
                100L
        );

        when(commentService.getComments(eventId, 0, 10))
                .thenReturn(List.of(comment));

        mockMvc.perform(get(URL_EVENTS + "/{eventId}" + URL_COMMENTS, eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].text").value("Комментарий"))
                .andExpect(jsonPath("$[0].event_id").value(eventId))
                .andExpect(jsonPath("$[0].author_id").value(100L))
                .andExpect(jsonPath("$[0].state").value("PENDING"));

        verify(commentService).getComments(eventId, 0, 10);
    }

    @Test
    @DisplayName("GET /events/{eventId}/comments/{commentId} должен вернуть один комментарий")
    void getComment_shouldReturnComment() throws Exception {
        long eventId = 1L;
        long commentId = 10L;

        CommentDto comment = makeCommentDto(
                commentId,
                "Один комментарий",
                eventId,
                100L
        );

        when(commentService.getCommentByEventId(eventId, commentId))
                .thenReturn(comment);

        mockMvc.perform(get(URL_EVENTS + "/{eventId}" + URL_COMMENTS + "/{commentId}", eventId, commentId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value("Один комментарий"))
                .andExpect(jsonPath("$.event_id").value(eventId))
                .andExpect(jsonPath("$.author_id").value(100L))
                .andExpect(jsonPath("$.state").value("PENDING"))
                .andExpect(jsonPath("$.moderator_id").value(200L))
                .andExpect(jsonPath("$.moderation_reason").value("Комментарий опубликован"));

        verify(commentService).getCommentByEventId(eventId, commentId);
    }

    @Test
    @DisplayName("GET /events/{eventId}/comments с eventId=0 должен вернуть 400")
    void getComments_whenEventIdIsZero_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(URL_EVENTS + "/{eventId}" + URL_COMMENTS, 0)
                        .param(PARAM_FROM, "0")
                        .param(PARAM_SIZE, "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("GET /events/{eventId}/comments с отрицательным eventId должен вернуть 400")
    void getComments_whenEventIdIsNegative_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(URL_EVENTS + "/{eventId}" + URL_COMMENTS, -1)
                        .param(PARAM_FROM, "0")
                        .param(PARAM_SIZE, "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("GET /events/{eventId}/comments с from < 0 должен вернуть 400")
    void getComments_whenFromIsNegative_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(URL_EVENTS + "/{eventId}" + URL_COMMENTS, 1)
                        .param(PARAM_FROM, "-1")
                        .param(PARAM_SIZE, "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("GET /events/{eventId}/comments с size=0 должен вернуть 400")
    void getComments_whenSizeIsZero_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(URL_EVENTS + "/{eventId}" + URL_COMMENTS, 1)
                        .param(PARAM_FROM, "0")
                        .param(PARAM_SIZE, "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("GET /events/{eventId}/comments с size < 0 должен вернуть 400")
    void getComments_whenSizeIsNegative_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(URL_EVENTS + "/{eventId}" + URL_COMMENTS, 1)
                        .param(PARAM_FROM, "0")
                        .param(PARAM_SIZE, "-10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    private CommentDto makeCommentDto(long id, String text, long eventId, long authorId) {
        return CommentDto.builder()
                .id(id)
                .text(text)
                .created(LocalDateTime.of(2024, 1, 1, 12, 0))
                .updated(LocalDateTime.of(2024, 1, 1, 13, 0))
                .event_id(eventId)
                .author_id(authorId)
                .status(CommentStatus.PENDING)
                .moderator_id(200L)
                .moderated(LocalDateTime.of(2024, 1, 1, 14, 0))
                .moderation_reason("Комментарий опубликован")
                .build();
    }
}
