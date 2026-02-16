package com.app.uteq.Services.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Entity.DocumentsGenerated;
import com.app.uteq.Repository.IDocumentsGeneratedRepository;
import com.app.uteq.Services.IDocumentsGeneratedService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentsGeneratedServiceImpl implements IDocumentsGeneratedService {

    private final IDocumentsGeneratedRepository documentsGeneratedRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentsGenerated> findAll() {
        return documentsGeneratedRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
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
