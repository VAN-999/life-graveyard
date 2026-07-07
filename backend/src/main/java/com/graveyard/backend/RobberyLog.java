package com.graveyard.backend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "robbery_logs")
public class RobberyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "robber_id", nullable = false)
    private Long robberId;

    @Column(name = "victim_id", nullable = false)
    private Long victimId;

    @Column(name = "decoration_id")
    private Long decorationId;

    @Column(name = "decoration_name", length = 100)
    private String decorationName;

    @Column(name = "success")
    private Boolean success = false;

    @Column(name = "penalty_type", length = 20)
    private String penaltyType; // LOST_DECORATION, LOST_MONEY, POOR_EXEMPT

    @Column(name = "penalty_amount")
    private Integer penaltyAmount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public RobberyLog() {
        this.createdAt = LocalDateTime.now();
    }

    public RobberyLog(Long robberId, Long victimId, Long decorationId, String decorationName, Boolean success, String penaltyType, Integer penaltyAmount) {
        this.robberId = robberId;
        this.victimId = victimId;
        this.decorationId = decorationId;
        this.decorationName = decorationName;
        this.success = success;
        this.penaltyType = penaltyType;
        this.penaltyAmount = penaltyAmount;
        this.createdAt = LocalDateTime.now();
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRobberId() { return robberId; }
    public void setRobberId(Long robberId) { this.robberId = robberId; }

    public Long getVictimId() { return victimId; }
    public void setVictimId(Long victimId) { this.victimId = victimId; }

    public Long getDecorationId() { return decorationId; }
    public void setDecorationId(Long decorationId) { this.decorationId = decorationId; }

    public String getDecorationName() { return decorationName; }
    public void setDecorationName(String decorationName) { this.decorationName = decorationName; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getPenaltyType() { return penaltyType; }
    public void setPenaltyType(String penaltyType) { this.penaltyType = penaltyType; }

    public Integer getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(Integer penaltyAmount) { this.penaltyAmount = penaltyAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}