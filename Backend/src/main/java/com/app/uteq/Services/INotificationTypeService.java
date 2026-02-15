package com.app.uteq.Services;

import com.app.uteq.Entity.NotificationType;

import java.util.List;
import java.util.Optional;

public interface INotificationTypeService {
    List<NotificationType> findAll();

    Optional<NotificationType> findById(Integer id);

    NotificationType save(NotificationType notificationType);

    void deleteById(Integer id);
}
