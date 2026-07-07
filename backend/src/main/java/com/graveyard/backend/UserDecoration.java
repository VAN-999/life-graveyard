package com.graveyard.backend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_decorations")
public class UserDecoration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "decoration_id", nullable = false)
    private Long decorationId;

    @Column(name = "is_equipped")
    private Boolean isEquipped = false;

    @Column(name = "obtained_at", updatable = false)
    private LocalDateTime obtainedAt;

    // 注意：这里彻底删掉了 quantity 字段

    public UserDecoration() {
        this.obtainedAt = LocalDateTime.now();
    }

    public UserDecoration(Long userId, Long decorationId) {
        this.userId = userId;
        this.decorationId = decorationId;
        this.isEquipped = false;
        this.obtainedAt = LocalDateTime.now();
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getDecorationId() { return decorationId; }
    public void setDecorationId(Long decorationId) { this.decorationId = decorationId; }

    public Boolean getIsEquipped() { return isEquipped; }
    public void setIsEquipped(Boolean isEquipped) { this.isEquipped = isEquipped; }

    public LocalDateTime getObtainedAt() { return obtainedAt; }
    public void setObtainedAt(LocalDateTime obtainedAt) { this.obtainedAt = obtainedAt; }
}