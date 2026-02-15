package com.app.uteq.Services;

import com.app.uteq.Dtos.CDocumentTemplateRequest;
import com.app.uteq.Dtos.DocumentTemplateResponse;
import com.app.uteq.Dtos.UDocumentTemplateRequest;

import java.util.List;

public interface IDocumentTemplatesService {
    void createDocumenttemplate(CDocumentTemplateRequest request);
    void updateDocumenttemplate(UDocumentTemplateRequest request);
    void deleteDocumenttemplate(Integer idtemplate);
    List<DocumentTemplateResponse> listDocumenttemplate(Boolean onlyActive);
}
