package com.example.qrattendance.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class AttendanceSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;
    
    private LocalDateTime sessionDate = LocalDateTime.now();
    
    @Column(name = "active")
    private boolean active = true;
}
