package ru.practicum.explorewithme.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.User;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class CommentDto {
    private long id;
    private String text;
    private LocalDateTime created;
    private LocalDateTime updated;
    private long event_id;
    private long author_id;

    @JsonProperty("state")
    private CommentStatus status;

    private long moderator_id;
    private LocalDateTime moderated;
    private String moderation_reason;
}
