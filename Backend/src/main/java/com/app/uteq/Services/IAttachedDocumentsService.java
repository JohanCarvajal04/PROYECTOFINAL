package com.app.uteq.Services;

import com.app.uteq.Entity.AttachedDocuments;
import java.util.List;
import java.util.Optional;

public interface IAttachedDocumentsService {
    List<AttachedDocuments> findAll();

    Optional<AttachedDocuments> findById(Integer id);

    AttachedDocuments save(AttachedDocuments attachedDocuments);

    void deleteById(Integer id);
}
