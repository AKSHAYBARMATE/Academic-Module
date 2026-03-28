package com.academic.repository;

import com.academic.entity.MarksheetSubjectComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarksheetSubjectComponentRepository extends JpaRepository<MarksheetSubjectComponent, Integer> {
    Optional<MarksheetSubjectComponent> findBySubject_IdAndComponent_Id(Integer subjectMarksId, Integer componentId);
}
