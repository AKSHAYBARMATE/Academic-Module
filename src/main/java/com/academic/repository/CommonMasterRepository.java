package com.academic.repository;

import com.academic.entity.CommonMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CommonMasterRepository extends JpaRepository<CommonMaster, Integer> {
    List<CommonMaster> findByIdInAndStatusTrue(List<Integer> ids);

    Optional<CommonMaster> findByIdAndStatusTrue(Integer id);

    boolean existsByIdAndStatusTrue(Integer classId);

    Optional<CommonMaster> findByCommonMasterKeyAndStatusTrue(String key);

    /**
     * Find a CommonMaster record whose 'data' field matches the given value.
     * Used to resolve academic year CommonMaster IDs from session strings like
     * "2025-26".
     */
    Optional<CommonMaster> findByDataAndStatusTrue(String data);
}
