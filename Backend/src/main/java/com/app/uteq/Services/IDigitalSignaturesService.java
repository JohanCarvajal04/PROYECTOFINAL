package com.app.uteq.Services;

import com.app.uteq.Entity.DigitalSignatures;
import java.util.List;
import java.util.Optional;

public interface IDigitalSignaturesService {
    List<DigitalSignatures> findAll();

    Optional<DigitalSignatures> findById(Integer id);

    DigitalSignatures save(DigitalSignatures digitalSignatures);

    void deleteById(Integer id);
}
