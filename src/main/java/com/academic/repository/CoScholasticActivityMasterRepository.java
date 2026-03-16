package com.academic.repository;


import com.academic.entity.CoScholasticActivityMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoScholasticActivityMasterRepository
        extends JpaRepository<CoScholasticActivityMaster, Long> {

    List<CoScholasticActivityMaster> findByIsActiveTrueOrderByDisplayOrder();

}