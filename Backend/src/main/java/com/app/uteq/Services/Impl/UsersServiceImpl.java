package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.Users;
import com.app.uteq.Repository.IUsersRepository;
import com.app.uteq.Services.IUsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements IUsersService {
    private final IUsersRepository repository;

    @Override
    public List<Users> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Users> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Users save(Users user) {
        return repository.save(user);
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}
