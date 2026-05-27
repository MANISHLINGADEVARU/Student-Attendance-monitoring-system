package com.example.qrattendance.controller;

import com.example.qrattendance.model.AttendanceRecord;
import com.example.qrattendance.model.Student;
import com.example.qrattendance.repository.AttendanceRecordRepository;
import com.example.qrattendance.repository.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/student")
public class StudentPortalController {

    private final StudentRepository studentRepository;
    private final AttendanceRecordRepository recordRepository;

    public StudentPortalController(StudentRepository studentRepository, AttendanceRecordRepository recordRepository) {
        this.studentRepository = studentRepository;
        this.recordRepository = recordRepository;
    }

    @GetMapping
    public String studentPortalHome() {
        return "student-portal";
    }

    @GetMapping("/dashboard")
    public String studentDashboard(@RequestParam String rollNumber, Model model, RedirectAttributes redirectAttributes) {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Roll number cannot be empty!");
            return "redirect:/student";
        }

        String trimmedRoll = rollNumber.trim();
        Optional<Student> studentOpt = studentRepository.findByRollNumber(trimmedRoll);
        if (studentOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No student record found with Roll Number: " + trimmedRoll);
            return "redirect:/student";
        }

        Student student = studentOpt.get();
        List<AttendanceRecord> records = recordRepository.findByStudent(student);

        model.addAttribute("student", student);
        model.addAttribute("records", records);
        model.addAttribute("attendanceCount", records.size());

        return "student-dashboard";
    }
}
