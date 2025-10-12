package com.academic.repository;


import com.academic.entity.CommonMaster;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CommonMasterRepository extends JpaRepository<CommonMaster, Integer> {
    List<CommonMaster> findByIdInAndStatusTrue(List<Integer> ids);

    Optional<CommonMaster> findByIdAndStatusTrue(Integer id);

    boolean existsByIdAndStatusTrue(Integer classId);
}
