package com.app.uteq.Services;

import com.app.uteq.Entity.SessionToken;
import java.util.List;
import java.util.Optional;

public interface ISessionTokenService {
    List<SessionToken> findAll();

    Optional<SessionToken> findById(Integer id);

    SessionToken save(SessionToken sessionToken);

    void deleteById(Integer id);
}
