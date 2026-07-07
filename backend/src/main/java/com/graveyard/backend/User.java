package com.graveyard.backend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "grave_style", length = 50)
    private String graveStyle = "CLASSIC";

    @Column(length = 20)
    private String status = "ALIVE";

    @Column(name = "level")
    private Integer level = 1;

    @Column(name = "hell_money")
    private Integer hellMoney = 100;

    @Column(name = "experience")
    private Integer experience = 0;

    public User() {}

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.graveStyle = "CLASSIC";
        this.status = "ALIVE";
        this.level = 1;
        this.hellMoney = 100;
        this.experience = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getGraveStyle() { return graveStyle; }
    public void setGraveStyle(String graveStyle) { this.graveStyle = graveStyle; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getHellMoney() { return hellMoney; }
    public void setHellMoney(Integer hellMoney) { this.hellMoney = hellMoney; }

    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }
}