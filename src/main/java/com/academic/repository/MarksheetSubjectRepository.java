package com.academic.repository;

import com.academic.entity.MarksheetSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarksheetSubjectRepository extends JpaRepository<MarksheetSubject, Integer> {
    Optional<MarksheetSubject> findByMarksheet_IdAndSubjectId(Integer marksheetId, Integer subjectId);
}
