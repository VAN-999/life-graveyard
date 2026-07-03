package com.graveyard.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GraveRepository extends JpaRepository<Grave, Long> {
    Optional<Grave> findByUserId(Long userId);
}