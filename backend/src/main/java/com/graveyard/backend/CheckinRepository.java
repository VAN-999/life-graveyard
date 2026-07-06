package com.graveyard.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    Optional<Checkin> findByUserIdAndCheckinDate(Long userId, LocalDate checkinDate);

    boolean existsByUserIdAndCheckinDate(Long userId, LocalDate checkinDate);

    List<Checkin> findByUserIdAndCheckinDateBetween(Long userId, LocalDate start, LocalDate end);

    int countByUserIdAndCheckinDateBetween(Long userId, LocalDate start, LocalDate end);

    int countByUserId(Long userId);
}