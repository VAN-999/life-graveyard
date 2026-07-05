package com.graveyard.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "decoration_states")
public class DecorationState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_decoration_id", nullable = false)
    private Long userDecorationId;

    @Column(name = "x")
    private Double x = 50.0;  // 百分比位置

    @Column(name = "y")
    private Double y = 50.0;

    @Column(name = "rotation")
    private Double rotation = 0.0;

    @Column(name = "scale")
    private Double scale = 1.0;

    @Column(name = "z_index")
    private Integer zIndex = 0;

    @Column(name = "is_visible")
    private Boolean isVisible = true;

    // 构造方法
    public DecorationState() {}

    public DecorationState(Long userId, Long userDecorationId) {
        this.userId = userId;
        this.userDecorationId = userDecorationId;
        this.x = 50.0 + (Math.random() - 0.5) * 30;  // 随机初始位置
        this.y = 50.0 + (Math.random() - 0.5) * 30;
        this.rotation = 0.0;
        this.scale = 1.0;
        this.zIndex = 0;
        this.isVisible = true;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getUserDecorationId() { return userDecorationId; }
    public void setUserDecorationId(Long userDecorationId) { this.userDecorationId = userDecorationId; }

    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }

    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }

    public Double getRotation() { return rotation; }
    public void setRotation(Double rotation) { this.rotation = rotation; }

    public Double getScale() { return scale; }
    public void setScale(Double scale) { this.scale = scale; }

    public Integer getZIndex() { return zIndex; }
    public void setZIndex(Integer zIndex) { this.zIndex = zIndex; }

    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }
}