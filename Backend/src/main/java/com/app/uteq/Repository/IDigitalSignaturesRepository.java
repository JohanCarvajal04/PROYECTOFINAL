package com.app.uteq.Repository;

import com.app.uteq.Entity.DigitalSignatures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDigitalSignaturesRepository extends JpaRepository<DigitalSignatures, Integer> {
}
