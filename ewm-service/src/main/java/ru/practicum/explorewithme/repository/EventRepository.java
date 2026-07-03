package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByCategoryId(long categoryId);
}
