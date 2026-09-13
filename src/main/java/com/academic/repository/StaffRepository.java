package com.academic.repository;

import com.academic.entity.Staff;
import com.academic.entity.StaffPunchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Staff findByIsDeletedAndId(boolean b, Long classTeacherId);

    Optional<Staff> findByIdAndIsDeletedFalse(Long id);

    default Optional<Staff> findByIdAndIsDeletedFalse(Integer id) {
        return id != null ? findByIdAndIsDeletedFalse(id.longValue()) : Optional.empty();
    }

    Optional<Staff> findByStaffCodeIgnoreCaseAndIsDeletedFalse(String staffCode);
}
