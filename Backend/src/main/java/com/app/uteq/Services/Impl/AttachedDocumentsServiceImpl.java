package com.app.uteq.Services.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Entity.AttachedDocuments;
import com.app.uteq.Repository.IAttachedDocumentsRepository;
import com.app.uteq.Services.IAttachedDocumentsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AttachedDocumentsServiceImpl implements IAttachedDocumentsService {

    private final IAttachedDocumentsRepository attachedDocumentsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AttachedDocuments> findAll() {
        return attachedDocumentsRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AttachedDocuments> findById(Integer id) {
        return attachedDocumentsRepository.findById(id);
    }

    @Override
    public AttachedDocuments save(AttachedDocuments attachedDocuments) {
        return attachedDocumentsRepository.save(attachedDocuments);
    }

    @Override
    public void deleteById(Integer id) {
        attachedDocumentsRepository.deleteById(id);
    }
}
