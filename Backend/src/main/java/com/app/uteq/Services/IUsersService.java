package com.app.uteq.Services;

import com.app.uteq.Entity.Users;

import java.util.List;
import java.util.Optional;

public interface IUsersService {
    List<Users> findAll();

    Optional<Users> findById(Integer id);

    Users save(Users user);

    void deleteById(Integer id);
}
