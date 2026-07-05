package ru.practicum.explorewithme.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class UpdateRequestStatusDto {
    List<Long> requestIds;
    RequestStatus status;
}
