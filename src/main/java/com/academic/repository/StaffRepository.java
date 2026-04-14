package com.academic.repository;

import com.academic.entity.Staff;
import com.academic.entity.StaffPunchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
}
