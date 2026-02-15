package com.app.uteq.Services.Impl;

import com.app.uteq.Entity.Students;
import com.app.uteq.Repository.IStudentsRepository;
import com.app.uteq.Services.IStudentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentsServiceImpl implements IStudentsService {

    @Autowired
    private IStudentsRepository studentsRepository;

    @Override
    public List<Students> findAll() {
        return studentsRepository.findAll();
    }

    @Override
    public Optional<Students> findById(Integer id) {
        return studentsRepository.findById(id);
    }

    @Override
    public Students save(Students students) {
        return studentsRepository.save(students);
    }

    @Override
    public void deleteById(Integer id) {
        studentsRepository.deleteById(id);
    }
}
