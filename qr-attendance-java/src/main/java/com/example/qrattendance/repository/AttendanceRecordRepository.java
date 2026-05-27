package com.example.qrattendance.repository;

import com.example.qrattendance.model.AttendanceRecord;
import com.example.qrattendance.model.AttendanceSession;
import com.example.qrattendance.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findBySession(AttendanceSession session);
    boolean existsByStudentAndSession(Student student, AttendanceSession session);
    
    @org.springframework.transaction.annotation.Transactional
    void deleteByStudent(Student student);
    
    List<AttendanceRecord> findByStudent(Student student);
}
