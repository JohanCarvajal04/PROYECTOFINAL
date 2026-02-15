package com.app.uteq.Services;

import com.app.uteq.Entity.DocumentsGenerated;
import java.util.List;
import java.util.Optional;

public interface IDocumentsGeneratedService {
    List<DocumentsGenerated> findAll();

    Optional<DocumentsGenerated> findById(Integer id);

    DocumentsGenerated save(DocumentsGenerated documentsGenerated);

    void deleteById(Integer id);
}
