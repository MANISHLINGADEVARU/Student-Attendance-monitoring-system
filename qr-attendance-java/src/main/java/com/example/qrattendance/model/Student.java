package com.example.qrattendance.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @Column(unique = true)
    private String rollNumber;
}
