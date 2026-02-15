package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.AttachedDocuments;
import com.app.uteq.Repository.IAttachedDocumentsRepository;
import com.app.uteq.Services.IAttachedDocumentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AttachedDocumentsServiceImpl implements IAttachedDocumentsService {

    @Autowired
    private IAttachedDocumentsRepository attachedDocumentsRepository;

    @Override
    public List<AttachedDocuments> findAll() {
        return attachedDocumentsRepository.findAll();
    }

    @Override
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
