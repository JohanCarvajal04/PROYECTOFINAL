package com.app.uteq.Services.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Dtos.CProcessingStageRequest;
import com.app.uteq.Dtos.ProcessingStageResponse;
import com.app.uteq.Dtos.UProcessingStageRequest;
import com.app.uteq.Repository.IProcessingStageRepository;
import com.app.uteq.Services.IProcessingStageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProcessingStageServiceImpl implements IProcessingStageService {
    private final IProcessingStageRepository repository;

    @Override
    public void createProcessingstage(CProcessingStageRequest request) {
        repository.spiProcessingStage(
                request.getStagename(),
                request.getStagecode(),
                request.getStagedescription(),
                request.getStageorder(),
                request.getRequiresapproval(),
                request.getMaxdurationdays()
        );
    }

    @Override
    public void updateProcessingstage(UProcessingStageRequest request) {
        repository.spuProcessingStage(
                request.getIdprocessingstage(),
                request.getStagename(),
                request.getStagecode(),
                request.getStagedescription(),
                request.getStageorder(),
                request.getRequiresapproval(),
                request.getMaxdurationdays()
        );
    }

    @Override
    public void deleteProcessingstage(Integer idprocessingstage) {
        repository.spdProcessingStage(idprocessingstage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessingStageResponse> listProcessingstage() {
        List<Object[]> rows = repository.fnListProcessingStage();

        return rows.stream().map(r -> new ProcessingStageResponse(
                toInt(r[0]),
                toStr(r[1]),
                toStr(r[2]),
                toStr(r[3]),
                toInt(r[4]),
                toBool(r[5]),
                toInt(r[6])
        )).toList();
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue(); // por si viene Long/BigInteger
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
