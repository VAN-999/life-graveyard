package com.graveyard.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "user_grave")
public class Grave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "tombstone_style", length = 50)
    private String tombstoneStyle = "CLASSIC";

    @Column(name = "pet_id")
    private Long petId;  // 当前装备的宠物ID

    @Column(name = "effect_id")
    private Long effectId;  // 当前装备的特效ID

    @Column(name = "background_music", length = 50)
    private String backgroundMusic = "SILENCE";

    @Column(name = "visit_count")
    private Integer visitCount = 0;

    public Grave() {}

    public Grave(Long userId) {
        this.userId = userId;
        this.tombstoneStyle = "CLASSIC";
        this.visitCount = 0;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTombstoneStyle() { return tombstoneStyle; }
    public void setTombstoneStyle(String tombstoneStyle) { this.tombstoneStyle = tombstoneStyle; }

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }

    public Long getEffectId() { return effectId; }
    public void setEffectId(Long effectId) { this.effectId = effectId; }

    public String getBackgroundMusic() { return backgroundMusic; }
    public void setBackgroundMusic(String backgroundMusic) { this.backgroundMusic = backgroundMusic; }

    public Integer getVisitCount() { return visitCount; }
    public void setVisitCount(Integer visitCount) { this.visitCount = visitCount; }
}