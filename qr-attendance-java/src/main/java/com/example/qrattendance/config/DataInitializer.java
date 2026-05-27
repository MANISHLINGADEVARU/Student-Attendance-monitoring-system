package com.example.qrattendance.config;

import com.example.qrattendance.model.Student;
import com.example.qrattendance.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(StudentRepository studentRepository) {
        return args -> {
            if (studentRepository.count() == 0) {
                Student s1 = new Student();
                s1.setName("Alice Smith");
                s1.setRollNumber("101");
                
                Student s2 = new Student();
                s2.setName("Bob Johnson");
                s2.setRollNumber("102");
                
                Student s3 = new Student();
                s3.setName("Charlie Brown");
                s3.setRollNumber("103");
                
                studentRepository.saveAll(Arrays.asList(s1, s2, s3));
            }
        };
    }
}
