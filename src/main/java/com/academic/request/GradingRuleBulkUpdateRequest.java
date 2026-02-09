package com.academic.request;

import lombok.Data;
import java.util.List;

@Data
public class GradingRuleBulkUpdateRequest {

    private List<GradingRuleUpdateItem> rules;
}
