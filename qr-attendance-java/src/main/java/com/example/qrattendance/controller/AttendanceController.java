package com.example.qrattendance.controller;

import com.example.qrattendance.model.AttendanceRecord;
import com.example.qrattendance.model.AttendanceSession;
import com.example.qrattendance.model.Student;
import com.example.qrattendance.repository.AttendanceRecordRepository;
import com.example.qrattendance.repository.AttendanceSessionRepository;
import com.example.qrattendance.repository.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class AttendanceController {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final StudentRepository studentRepository;

    public AttendanceController(AttendanceSessionRepository sessionRepository,
                                AttendanceRecordRepository recordRepository,
                                StudentRepository studentRepository) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/")
    public String landing() {
        return "landing";
    }

    @GetMapping("/admin")
    public String index(Model model) {
        model.addAttribute("sessions", sessionRepository.findAll());
        return "index";
    }

    @PostMapping("/admin/session/create")
    public String createSession(@RequestParam String subject) {
        AttendanceSession session = new AttendanceSession();
        session.setSubject(subject);
        sessionRepository.save(session);
        return "redirect:/admin";
    }

    @GetMapping("/admin/session/{id}")
    public String viewSession(@PathVariable Long id, Model model) {
        AttendanceSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session Id:" + id));
        List<AttendanceRecord> records = recordRepository.findBySession(session);
        model.addAttribute("attendanceSession", session);
        model.addAttribute("records", records);
        return "session-detail";
    }

    @GetMapping("/mark-attendance/{sessionId}")
    public String markAttendancePage(@PathVariable Long sessionId, Model model) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session Id:" + sessionId));
        if (!session.isActive()) {
            return "session-closed";
        }
        model.addAttribute("attendanceSession", session);
        return "mark-attendance";
    }

    @PostMapping("/mark-attendance/{sessionId}")
    public String submitAttendance(@PathVariable Long sessionId, 
                                   @RequestParam String rollNumber,
                                   RedirectAttributes redirectAttributes) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session Id:" + sessionId));
        
        if (!session.isActive()) {
            redirectAttributes.addFlashAttribute("error", "Session is closed!");
            return "redirect:/mark-attendance/" + sessionId;
        }

        Optional<Student> studentOpt = studentRepository.findByRollNumber(rollNumber);
        if (studentOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Student not found with Roll Number: " + rollNumber);
            return "redirect:/mark-attendance/" + sessionId;
        }

        Student student = studentOpt.get();
        if (recordRepository.existsByStudentAndSession(student, session)) {
            redirectAttributes.addFlashAttribute("error", "Attendance already marked for this session!");
            return "redirect:/mark-attendance/" + sessionId;
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setSession(session);
        record.setStudent(student);
        recordRepository.save(record);

        redirectAttributes.addFlashAttribute("success", "Attendance marked successfully!");
        return "redirect:/mark-attendance/" + sessionId;
    }

    @PostMapping("/admin/session/{id}/toggle")
    public String toggleSessionStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        AttendanceSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session Id:" + id));
        session.setActive(!session.isActive());
        sessionRepository.save(session);
        
        String statusStr = session.isActive() ? "activated" : "deactivated";
        redirectAttributes.addFlashAttribute("success", "Session has been successfully " + statusStr + "!");
        return "redirect:/admin/session/" + id;
    }

    @GetMapping("/admin/session/{id}/export")
    public void exportAttendance(@PathVariable Long id, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        AttendanceSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session Id:" + id));
        List<AttendanceRecord> records = recordRepository.findBySession(session);

        String subjectClean = session.getSubject().replaceAll("[^a-zA-Z0-9_-]", "_");
        String filename = "attendance_session_" + id + "_" + subjectClean + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        // Write UTF-8 BOM first so Excel opens it with correct encoding
        response.getOutputStream().write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });

        java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(response.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8));
        
        // CSV Header
        writer.println("Roll Number,Student Name,Marked At");

        // CSV Rows
        for (AttendanceRecord record : records) {
            String roll = record.getStudent().getRollNumber();
            String name = record.getStudent().getName();
            String time = record.getMarkedAt().toString().replace("T", " ").substring(0, 19);
            
            // Escape values containing commas or quotes
            roll = escapeCsv(roll);
            name = escapeCsv(name);
            
            writer.println(roll + "," + name + "," + time);
        }
        
        writer.flush();
        writer.close();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n") || val.contains("\r")) {
            val = val.replace("\"", "\"\"");
            return "\"" + val + "\"";
        }
        return val;
    }
}
