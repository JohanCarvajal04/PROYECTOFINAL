package com.app.uteq.Services.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Dtos.CDeadlineRuleRequest;
import com.app.uteq.Dtos.DeadlineRuleResponse;
import com.app.uteq.Dtos.UDeadlineRuleRequest;
import com.app.uteq.Repository.IDeadLineRulesRepository;
import com.app.uteq.Services.IDeadLineRulesService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DeadLineRulesServiceImpl implements IDeadLineRulesService {
    private final IDeadLineRulesRepository repository;

    @Override
    public void createDeadlinerule(CDeadlineRuleRequest request) {
        repository.spiDeadlineRule(
                request.getRulename(),
                request.getProcedurecategory(),
                request.getBasedeadlinedays(),
                request.getWarningdaysbefore(),
                request.getActive()
        );
    }

    @Override
    public void updateDeadlinerule(UDeadlineRuleRequest request) {
        repository.spuDeadlineRule(
                request.getIddeadlinerule(),
                request.getRulename(),
                request.getProcedurecategory(),
                request.getBasedeadlinedays(),
                request.getWarningdaysbefore(),
                request.getActive()
        );
    }

    @Override
    public void deleteDeadlinerule(Integer iddeadlinerule) {
        repository.spdDeadlineRule(iddeadlinerule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeadlineRuleResponse> listDeadlinerule(Boolean onlyActive) {
        List<Object[]> rows = repository.fnListDeadlineRules(onlyActive);

        return rows.stream().map(r -> new DeadlineRuleResponse(
                toInt(r[0]),
                toStr(r[1]),
                toStr(r[2]),
                toInt(r[3]),
                toInt(r[4]),
                toBool(r[5])
        )).toList();
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue(); // por si viene BigInteger/Long
        throw new IllegalArgumentException("Tipo numérico no soportado: " + v.getClass());
    }

    private String toStr(Object v) {
        return v == null ? null : v.toString();
    }

    private Boolean toBool(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.intValue() != 0;
        return Boolean.parseBoolean(v.toString());
    }

}
