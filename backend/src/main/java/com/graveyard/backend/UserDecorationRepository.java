package com.graveyard.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDecorationRepository extends JpaRepository<UserDecoration, Long> {

    List<UserDecoration> findByUserId(Long userId);

    Optional<UserDecoration> findByUserIdAndDecorationId(Long userId, Long decorationId);

    List<UserDecoration> findByUserIdAndIsEquippedTrue(Long userId);

    @Query(value = "SELECT * FROM user_decorations WHERE user_id = :userId AND is_equipped = 1", nativeQuery = true)
    List<UserDecoration> findEquippedByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndDecorationId(Long userId, Long decorationId);
}