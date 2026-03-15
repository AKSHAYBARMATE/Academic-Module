package com.academic.repository;

import com.academic.entity.ExamComponentMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamComponentMasterRepostory extends JpaRepository<ExamComponentMaster, Integer> {
}
