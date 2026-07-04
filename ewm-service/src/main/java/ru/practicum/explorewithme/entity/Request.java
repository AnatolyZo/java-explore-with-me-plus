package ru.practicum.explorewithme.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.explorewithme.dto.RequestStatus;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "requests", schema = "public")
public class Request {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalDateTime created;

    private long eventId;

    private long requesterId;

    @Enumerated(EnumType.STRING)
    @Setter
    private RequestStatus status;
}
