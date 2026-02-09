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
@Transactional
public interface GradingRuleService {


    /* CREATE */
    public StandardResponse<?> create(GradingRuleRequest request) ;

    /* READ */
    public StandardResponse<?> getAll();

    /* UPDATE */
    public StandardResponse<?> update(Long id, GradingRuleRequest request);

    /* DELETE (Soft) */
    public StandardResponse<?> delete(Long id);


}
