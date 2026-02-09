package com.academic.repository;

import com.academic.entity.GradingRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradingRuleRepository
        extends JpaRepository<GradingRule, Long> {

    List<GradingRule> findByIsActiveTrueOrderByMaxPercentageDesc();
}
