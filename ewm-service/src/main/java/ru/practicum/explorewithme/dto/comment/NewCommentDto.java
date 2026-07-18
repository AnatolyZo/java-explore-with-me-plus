package ru.practicum.explorewithme.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class NewCommentDto {
    @NotBlank
    private String text;
    @NotNull
    @Positive
    private Long eventId;
    @NotNull
    @Positive
    private Long authorId;
}
