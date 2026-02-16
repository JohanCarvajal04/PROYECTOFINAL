package com.app.uteq.Services.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Entity.NotificationType;
import com.app.uteq.Repository.INotificationTypeRepository;
import com.app.uteq.Services.INotificationTypeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationTypeServiceImpl implements INotificationTypeService {
    private final INotificationTypeRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationType> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationType> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public NotificationType save(NotificationType notificationType) {
        return repository.save(notificationType);
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}
