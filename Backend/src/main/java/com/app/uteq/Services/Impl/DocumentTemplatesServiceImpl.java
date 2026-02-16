package com.app.uteq.Services.Impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.app.uteq.Dtos.CDocumentTemplateRequest;
import com.app.uteq.Dtos.DocumentTemplateResponse;
import com.app.uteq.Dtos.UDocumentTemplateRequest;
import com.app.uteq.Repository.IDocumentTemplatesRepository;
import com.app.uteq.Services.IDocumentTemplatesService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentTemplatesServiceImpl implements IDocumentTemplatesService {
    private final IDocumentTemplatesRepository repository;

    @Override
    public void createDocumenttemplate(CDocumentTemplateRequest request) {
        repository.spiDocumentTemplate(
                request.getTemplatename(),
                request.getTemplatecode(),
                request.getTemplatepath(),
                request.getDocumenttype(),
                request.getVersion(),
                request.getRequiressignature(),
                request.getActive());
    }

    @Override
    public void updateDocumenttemplate(UDocumentTemplateRequest request) {
        repository.spuDocumentTemplate(
                request.getIdtemplate(),
                request.getTemplatename(),
                request.getTemplatecode(),
                request.getTemplatepath(),
                request.getDocumenttype(),
                request.getVersion(),
                request.getRequiressignature(),
                request.getActive());
    }

    @Override
    public void deleteDocumenttemplate(Integer idtemplate) {
        repository.spdDocumentTemplate(idtemplate);
    }

    @Override
    public List<DocumentTemplateResponse> listDocumenttemplate(Boolean onlyActive) {
        List<Object[]> rows = repository.fnListDocumentTemplates(onlyActive);

        return rows.stream().map(r -> new DocumentTemplateResponse(
                toInt(r[0]),
                toStr(r[1]),
                toStr(r[2]),
                toStr(r[3]),
                toStr(r[4]),
                toStr(r[5]),
                toBool(r[6]),
                toBool(r[7]),
                toLocalDateTime(r[8]),
                toLocalDateTime(r[9]))).toList();
    }

    private Integer toInt(Object v) {
        if (v == null)
            return null;
        if (v instanceof Integer i)
            return i;
        if (v instanceof Number n)
            return n.intValue();
        throw new IllegalArgumentException("Tipo numérico no soportado: " + v.getClass());
    }

    private String toStr(Object v) {
        return v == null ? null : v.toString();
    }

    private Boolean toBool(Object v) {
        if (v == null)
            return null;
        if (v instanceof Boolean b)
            return b;
        if (v instanceof Number n)
            return n.intValue() != 0;
        return Boolean.parseBoolean(v.toString());
    }

    private LocalDateTime toLocalDateTime(Object v) {
        if (v == null)
            return null;
        if (v instanceof LocalDateTime ldt)
            return ldt;
        if (v instanceof Timestamp ts)
            return ts.toLocalDateTime();
        // por si llega como java.util.Date
        if (v instanceof java.util.Date d)
            return new Timestamp(d.getTime()).toLocalDateTime();
        throw new IllegalArgumentException("Tipo timestamp no soportado: " + v.getClass());
    }
}
