package ru.practicum.explorewithme.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.explorewithme.dto.RequestStatus;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "requests", schema = "public")
public class Request {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "event_id")
    private long eventId;

    @Column(name = "requester_id")
    private long requesterId;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}
