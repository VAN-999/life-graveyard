package com.graveyard.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(name = "reward_money", nullable = false)
    private Integer rewardMoney;

    @Column(name = "condition_type", nullable = false, length = 30)
    private String conditionType;

    @Column(name = "condition_value")
    private Integer conditionValue;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public Task() {}

    public Task(String name, String description, Integer rewardMoney, String conditionType, Integer conditionValue) {
        this.name = name;
        this.description = description;
        this.rewardMoney = rewardMoney;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.isActive = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getRewardMoney() { return rewardMoney; }
    public void setRewardMoney(Integer rewardMoney) { this.rewardMoney = rewardMoney; }

    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }

    public Integer getConditionValue() { return conditionValue; }
    public void setConditionValue(Integer conditionValue) { this.conditionValue = conditionValue; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}