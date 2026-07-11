package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.entity.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByEventId(long eventId);

    List<Request> findByRequesterIdIn(List<Long> requestIds);

    List<Request> findAllByRequesterIdOrderByCreatedDesc(Long requesterId);

    Optional<Request> findByEventIdAndRequesterId(long eventId, long userId);

    int countByEventId(long eventId);
}