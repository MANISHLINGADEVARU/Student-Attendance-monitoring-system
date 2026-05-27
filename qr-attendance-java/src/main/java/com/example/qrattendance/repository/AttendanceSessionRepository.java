package com.example.qrattendance.repository;

import com.example.qrattendance.model.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
}
