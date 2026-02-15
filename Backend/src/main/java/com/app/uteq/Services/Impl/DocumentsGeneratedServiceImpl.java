package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.DocumentsGenerated;
import com.app.uteq.Repository.IDocumentsGeneratedRepository;
import com.app.uteq.Services.IDocumentsGeneratedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentsGeneratedServiceImpl implements IDocumentsGeneratedService {

    @Autowired
    private IDocumentsGeneratedRepository documentsGeneratedRepository;

    @Override
    public List<DocumentsGenerated> findAll() {
        return documentsGeneratedRepository.findAll();
    }

    @Override
    public Optional<DocumentsGenerated> findById(Integer id) {
        return documentsGeneratedRepository.findById(id);
    }

    @Override
    public DocumentsGenerated save(DocumentsGenerated documentsGenerated) {
        return documentsGeneratedRepository.save(documentsGenerated);
    }

    @Override
    public void deleteById(Integer id) {
        documentsGeneratedRepository.deleteById(id);
    }
}
