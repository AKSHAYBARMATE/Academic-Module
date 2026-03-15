package com.academic.repository;

import com.academic.entity.ExamComponentMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ExamComponentMasterRepostory extends JpaRepository<ExamComponentMaster, Integer> {

    List<ExamComponentMaster> findByActiveTrueOrderByDisplayOrder();
}
