package com.graveyard.backend;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_tasks")
public class UserTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "assign_date", nullable = false)
    private LocalDate assignDate;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "is_claimed")
    private Boolean isClaimed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    public UserTask() {}

    public UserTask(Long userId, Long taskId, LocalDate assignDate) {
        this.userId = userId;
        this.taskId = taskId;
        this.assignDate = assignDate;
        this.isCompleted = false;
        this.isClaimed = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public LocalDate getAssignDate() { return assignDate; }
    public void setAssignDate(LocalDate assignDate) { this.assignDate = assignDate; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public Boolean getIsClaimed() { return isClaimed; }
    public void setIsClaimed(Boolean isClaimed) { this.isClaimed = isClaimed; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getClaimedAt() { return claimedAt; }
    public void setClaimedAt(LocalDateTime claimedAt) { this.claimedAt = claimedAt; }
}