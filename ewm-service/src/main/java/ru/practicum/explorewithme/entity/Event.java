package ru.practicum.explorewithme.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.dto.EventStatus;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "events", schema = "public")
public class Event {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "annotation", nullable = false)
    private String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "conf_req")
    private int confirmedRequests;

    @Column(name = "created", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Embedded
    private LocationEmbeddable location;

    @Column(name = "paid", nullable = false)
    private boolean paid;

    @Column(name = "participant_limit")
    private int participantLimit;

    @Column(name = "published", nullable = false)
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    private boolean requestModeration;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Column(name = "title", nullable = false)
    private String title;

    @Transient
    private long views;
}
