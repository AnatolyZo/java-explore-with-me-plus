package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
