package com.graveyard.backend;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_data")
public class DailyData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "steps")
    private Integer steps;

    @Column(name = "screen_time_minutes")
    private Integer screenTimeMinutes;

    @Column(name = "key_presses")
    private Integer keyPresses;

    @Column(name = "sleep_hours")
    private Double sleepHours;

    @Column(name = "app_opens")
    private Integer appOpens;

    @Column(name = "last_active_at")
    private String lastActiveAt;  // 存 "01:23" 这种格式

    @Column(name = "moments_viewed")
    private Boolean momentsViewed;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DailyData() {}

    // 构造方法（方便测试用）
    public DailyData(Long userId, LocalDate recordDate, Integer steps, Integer screenTimeMinutes) {
        this.userId = userId;
        this.recordDate = recordDate;
        this.steps = steps;
        this.screenTimeMinutes = screenTimeMinutes;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public Integer getSteps() { return steps; }
    public void setSteps(Integer steps) { this.steps = steps; }

    public Integer getScreenTimeMinutes() { return screenTimeMinutes; }
    public void setScreenTimeMinutes(Integer screenTimeMinutes) { this.screenTimeMinutes = screenTimeMinutes; }

    public Integer getKeyPresses() { return keyPresses; }
    public void setKeyPresses(Integer keyPresses) { this.keyPresses = keyPresses; }

    public Double getSleepHours() { return sleepHours; }
    public void setSleepHours(Double sleepHours) { this.sleepHours = sleepHours; }

    public Integer getAppOpens() { return appOpens; }
    public void setAppOpens(Integer appOpens) { this.appOpens = appOpens; }

    public String getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(String lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public Boolean getMomentsViewed() { return momentsViewed; }
    public void setMomentsViewed(Boolean momentsViewed) { this.momentsViewed = momentsViewed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}