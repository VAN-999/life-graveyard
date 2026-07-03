package com.graveyard.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyDataRepository extends JpaRepository<DailyData, Long> {

    Optional<DailyData> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);

    List<DailyData> findByUserIdOrderByRecordDateDesc(Long userId);

    @Query("SELECT DISTINCT d.userId FROM DailyData d WHERE d.recordDate = :date")
    List<Long> findDistinctUserIdsByRecordDate(@Param("date") LocalDate date);
}