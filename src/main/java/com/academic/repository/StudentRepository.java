package com.academic.repository;

import com.academic.entity.AcademicCalendarEvent;
import com.academic.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

// Spring Data JPA Repository for AcademicCalendarEvent
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<AcademicCalendarEvent> {

}
