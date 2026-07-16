package ru.practicum.explorewithme.dto.comment;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class CommentDto {
    private long id;
    private String text;
}
