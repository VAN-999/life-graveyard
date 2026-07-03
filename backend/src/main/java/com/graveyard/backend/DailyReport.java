package com.graveyard.backend;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_reports")
public class DailyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "death_reason", length = 500)
    private String deathReason;

    @Column(name = "epitaph", length = 1000)
    private String epitaph;

    @Column(name = "death_score")
    private Integer deathScore;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public DailyReport() {}

    public DailyReport(Long userId, LocalDate reportDate, String deathReason, String epitaph, Integer deathScore, String summary) {
        this.userId = userId;
        this.reportDate = reportDate;
        this.deathReason = deathReason;
        this.epitaph = epitaph;
        this.deathScore = deathScore;
        this.summary = summary;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public String getDeathReason() { return deathReason; }
    public void setDeathReason(String deathReason) { this.deathReason = deathReason; }

    public String getEpitaph() { return epitaph; }
    public void setEpitaph(String epitaph) { this.epitaph = epitaph; }

    public Integer getDeathScore() { return deathScore; }
    public void setDeathScore(Integer deathScore) { this.deathScore = deathScore; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}