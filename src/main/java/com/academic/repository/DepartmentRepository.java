package com.academic.repository;

import com.academic.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    Optional<Department> findByIdAndIsDeleteFalse(Integer id);

    Optional<Department> findByNameIgnoreCase(String name);

    Optional<Department> findByCodeIgnoreCase(String code);

    List<Department> findByIsDeleteFalseOrderByNameAsc();

    List<Department> findAllByOrderByNameAsc();
}
