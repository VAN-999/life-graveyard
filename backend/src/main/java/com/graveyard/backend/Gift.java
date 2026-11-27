package com.graveyard.backend;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "gifts")
public class Gift {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false) public Long senderId;
    @Column(nullable = false) public Long recipientId;
    @Column(nullable = false) public Long userDecorationId;
    @Column(nullable = false) public String decorationName;
    @Column(length = 200) public String message;
    @Column(nullable = false, unique = true, length = 36) public String requestId;
    @Column(nullable = false) public LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC);
}
