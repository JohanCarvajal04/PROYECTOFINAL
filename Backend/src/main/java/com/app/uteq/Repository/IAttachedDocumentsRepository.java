package com.app.uteq.Repository;

import com.app.uteq.Entity.AttachedDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAttachedDocumentsRepository extends JpaRepository<AttachedDocuments, Integer> {
}
