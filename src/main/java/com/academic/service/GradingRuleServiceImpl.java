package com.academic.service;

import com.academic.entity.GradingRule;
import com.academic.repository.GradingRuleRepository;
import com.academic.request.GradingRuleRequest;
import com.academic.response.GradingRuleResponse;
import com.academic.response.StandardResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class GradingRuleServiceImpl implements GradingRuleService{

    private final GradingRuleRepository repository;

    /* CREATE */
    public StandardResponse<?> create(GradingRuleRequest request) {

        validateRange(request.getMinPercentage(), request.getMaxPercentage());

        GradingRule rule = GradingRule.builder()
                .gradeName(request.getGradeName())
                .gradePoint(request.getGradePoint())
                .minPercentage(request.getMinPercentage())
                .maxPercentage(request.getMaxPercentage())
                .description(request.getDescription())
                .build();

        repository.save(rule);

        return StandardResponse.success(map(rule), "Grading rule created");
    }

    /* READ */
    public StandardResponse<?> getAll() {
        return StandardResponse.success(
                repository.findByIsActiveTrueOrderByMaxPercentageDesc()
                        .stream().map(this::map).toList(),
                "Grading rules fetched"
        );
    }

    /* UPDATE */
    public StandardResponse<?> update(Long id, GradingRuleRequest request) {

        GradingRule rule = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found"));

        validateRange(request.getMinPercentage(), request.getMaxPercentage());

        rule.setGradePoint(request.getGradePoint());
        rule.setMinPercentage(request.getMinPercentage());
        rule.setMaxPercentage(request.getMaxPercentage());
        rule.setDescription(request.getDescription());

        repository.save(rule);

        return StandardResponse.success(map(rule), "Grading rule updated");
    }

    /* DELETE (Soft) */
    public StandardResponse<?> delete(Long id) {

        GradingRule rule = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found"));

        rule.setIsActive(false);
        repository.save(rule);

        return StandardResponse.success("Grading rule deleted");
    }

    /* ================= HELPERS ================= */

    private void validateRange(int min, int max) {
        if (min < 0 || max > 100 || min >= max) {
            throw new RuntimeException("Invalid percentage range");
        }
    }

    private GradingRuleResponse map(GradingRule r) {
        return GradingRuleResponse.builder()
                .id(r.getId())
                .gradeName(r.getGradeName())
                .gradePoint(r.getGradePoint())
                .minPercentage(r.getMinPercentage())
                .maxPercentage(r.getMaxPercentage())
                .description(r.getDescription())
                .build();
    }
}
