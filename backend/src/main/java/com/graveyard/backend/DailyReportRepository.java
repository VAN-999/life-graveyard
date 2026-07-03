package com.graveyard.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

    Optional<DailyReport> findByUserIdAndReportDate(Long userId, LocalDate reportDate);

    boolean existsByUserIdAndReportDate(Long userId, LocalDate reportDate);
}