package com.academic.repository;

import com.academic.entity.ExamSubjectConfigComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ExamSubjectConfigComponentRepository extends JpaRepository<ExamSubjectConfigComponent,Integer> {

    ExamSubjectConfigComponent findByConfig_IdAndComponent_Id(Long id, Integer id1);
}
