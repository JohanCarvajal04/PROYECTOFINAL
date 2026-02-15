package com.app.uteq.Repository;

import com.app.uteq.Entity.DocumentsGenerated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDocumentsGeneratedRepository extends JpaRepository<DocumentsGenerated, Integer> {
}
