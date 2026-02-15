package com.app.uteq.Services;

import com.app.uteq.Entity.Credentials;
import java.util.List;
import java.util.Optional;

public interface ICredentialsService {
    List<Credentials> findAll();

    Optional<Credentials> findById(Integer id);

    Credentials save(Credentials credentials);

    void deleteById(Integer id);
}
