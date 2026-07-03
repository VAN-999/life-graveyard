package com.graveyard.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserTaskRepository extends JpaRepository<UserTask, Long> {
    List<UserTask> findByUserIdAndAssignDate(Long userId, LocalDate assignDate);

    Optional<UserTask> findByUserIdAndTaskIdAndAssignDate(Long userId, Long taskId, LocalDate assignDate);

    @Query("SELECT COUNT(u) FROM UserTask u WHERE u.userId = :userId AND u.assignDate = :date AND u.isCompleted = true")
    int countCompletedToday(@Param("userId") Long userId, @Param("date") LocalDate date);
}