package com.graveyard.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserDecorationRepository extends JpaRepository<UserDecoration, Long> {
    List<UserDecoration> findByUserId(Long userId);
    Optional<UserDecoration> findByUserIdAndDecorationId(Long userId, Long decorationId);
    List<UserDecoration> findByUserIdAndIsEquippedTrue(Long userId);
    boolean existsByUserIdAndDecorationId(Long userId, Long decorationId);
}