package com.app.uteq.Services.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Dtos.CRejectionReasonRequest;
import com.app.uteq.Dtos.RejectionReasonResponse;
import com.app.uteq.Dtos.URejectionReasonRequest;
import com.app.uteq.Repository.IRejectionReasonsRepository;
import com.app.uteq.Services.IRejectionReasonsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RejectionReasonsServiceImpl implements IRejectionReasonsService {

    private final IRejectionReasonsRepository repository;

    @Override
    public void createRejectreason(CRejectionReasonRequest request) {
        repository.spiRejectionReason(
                request.getReasoncode(),
                request.getReasondescription(),
                request.getCategory(),
                request.getActive()
        );
    }

    @Override
    public void updateRejectreason(URejectionReasonRequest request) {
        repository.spuRejectionReason(
                request.getIdrejectionreason(),
                request.getReasoncode(),
                request.getReasondescription(),
                request.getCategory(),
                request.getActive()
        );
    }

    @Override
    public void deleteRejectreason(Integer idrejectionreason) {
        repository.spdRejectionReason(idrejectionreason);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RejectionReasonResponse> listRejectreason(Boolean onlyActive) {
        List<Object[]> rows = repository.fnListRejectionReasons(onlyActive);

        return rows.stream().map(r -> new RejectionReasonResponse(
                toInt(r[0]),
                toStr(r[1]),
                toStr(r[2]),
                toStr(r[3]),
                toBool(r[4])
        )).toList();
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue();
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
