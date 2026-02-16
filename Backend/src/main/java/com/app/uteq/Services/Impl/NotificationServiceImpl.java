package com.app.uteq.Services.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.uteq.Entity.Notification;
import com.app.uteq.Repository.INotificationRepository;
import com.app.uteq.Services.INotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements INotificationService {

    private final INotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> findById(Integer id) {
        return notificationRepository.findById(id);
    }

    @Override
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public void deleteById(Integer id) {
        notificationRepository.deleteById(id);
    }
}
