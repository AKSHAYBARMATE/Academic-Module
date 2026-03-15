package com.academic.repository;

import com.academic.entity.ExamSubjectConfigComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamSubjectConfigComponentRepository extends JpaRepository<ExamSubjectConfigComponent,Integer> {
}
