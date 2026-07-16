package ru.practicum.explorewithme.dto.comment;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class CommentShortDto {
    private long id;
    private String text;
}
