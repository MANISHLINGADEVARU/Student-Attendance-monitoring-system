package com.example.qrattendance.controller;

import com.example.qrattendance.model.Student;
import com.example.qrattendance.repository.AttendanceRecordRepository;
import com.example.qrattendance.repository.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final AttendanceRecordRepository recordRepository;

    public StudentController(StudentRepository studentRepository, AttendanceRecordRepository recordRepository) {
        this.studentRepository = studentRepository;
        this.recordRepository = recordRepository;
    }

    @GetMapping
    public String listStudents(Model model) {
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("newStudent", new Student());
        return "students";
    }

    @PostMapping("/add")
    public String addStudent(@ModelAttribute Student student, RedirectAttributes redirectAttributes) {
        if (student.getName() == null || student.getName().trim().isEmpty() ||
            student.getRollNumber() == null || student.getRollNumber().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Name and Roll Number cannot be empty!");
            return "redirect:/admin/students";
        }

        // Check for duplicate roll number
        Optional<Student> existingStudent = studentRepository.findByRollNumber(student.getRollNumber().trim());
        if (existingStudent.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Roll Number '" + student.getRollNumber() + "' is already assigned to " + existingStudent.get().getName() + "!");
            return "redirect:/admin/students";
        }

        student.setName(student.getName().trim());
        student.setRollNumber(student.getRollNumber().trim());
        studentRepository.save(student);
        redirectAttributes.addFlashAttribute("success", "Student registered successfully!");
        return "redirect:/admin/students";
    }

    @GetMapping("/edit/{id}")
    public String editStudentForm(@PathVariable Long id, Model model) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
        model.addAttribute("student", student);
        return "student-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateStudent(@PathVariable Long id, @ModelAttribute Student student, RedirectAttributes redirectAttributes) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));

        if (student.getName() == null || student.getName().trim().isEmpty() ||
            student.getRollNumber() == null || student.getRollNumber().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Name and Roll Number cannot be empty!");
            return "redirect:/admin/students/edit/" + id;
        }

        String newRoll = student.getRollNumber().trim();
        // Check for duplicate roll number (if changed)
        if (!existing.getRollNumber().equals(newRoll)) {
            Optional<Student> duplicate = studentRepository.findByRollNumber(newRoll);
            if (duplicate.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Roll Number '" + newRoll + "' is already assigned to " + duplicate.get().getName() + "!");
                return "redirect:/admin/students/edit/" + id;
            }
        }

        existing.setName(student.getName().trim());
        existing.setRollNumber(newRoll);
        studentRepository.save(existing);
        redirectAttributes.addFlashAttribute("success", "Student details updated successfully!");
        return "redirect:/admin/students";
    }

    @PostMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));

        // Cascade delete attendance records first to avoid foreign key violations
        recordRepository.deleteByStudent(student);
        
        studentRepository.delete(student);
        redirectAttributes.addFlashAttribute("success", "Student deleted successfully!");
        return "redirect:/admin/students";
    }
}
