package com.app.uteq.Services;

import com.app.uteq.Entity.Notification;
import java.util.List;
import java.util.Optional;

public interface INotificationService {
    List<Notification> findAll();

    Optional<Notification> findById(Integer id);

    Notification save(Notification notification);

    void deleteById(Integer id);
}
