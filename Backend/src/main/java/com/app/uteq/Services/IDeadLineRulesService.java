package com.app.uteq.Services;

import com.app.uteq.Dtos.CDeadlineRuleRequest;
import com.app.uteq.Dtos.DeadlineRuleResponse;
import com.app.uteq.Dtos.UDeadlineRuleRequest;

import java.util.List;

public interface IDeadLineRulesService {
    void createDeadlinerule(CDeadlineRuleRequest request);
    void updateDeadlinerule(UDeadlineRuleRequest request);
    void deleteDeadlinerule(Integer iddeadlinerule);
    List<DeadlineRuleResponse> listDeadlinerule(Boolean onlyActive);
}
